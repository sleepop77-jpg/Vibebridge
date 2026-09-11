package dev.vibebridge.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GitHubClient {

    private data class Raw(val code: Int, val body: String)

    companion object {
        private const val BASE = "https://api.github.com"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()

        fun httpError(code: Int, body: String): String {
            val msg = try { JSONObject(body).optString("message", "") } catch (e: Exception) { "" }
            return when (code) {
                401 -> "Token rejected (401). Regenerate a fine-grained PAT with Contents read and write."
                403 -> "Forbidden (403). Token lacks permission for this repository, or rate limit reached."
                404 -> "Not found (404). Check repository name, branch, and file paths."
                409 -> "Conflict (409). Branch moved since last sync. Pull or retry."
                422 -> "Unprocessable (422). " + if (msg.isNotBlank()) msg else "Tree or ref update rejected."
                else -> "HTTP $code. " + msg
            }
        }

        private fun enc(path: String): String =
            path.split("/").joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8") }
    }

    private suspend fun raw(pat: String, method: String, path: String, body: JSONObject?): VbResult<Raw> =
        withContext(Dispatchers.IO) {
            try {
                val b = Request.Builder()
                    .url(BASE + path)
                    .header("Authorization", "Bearer $pat")
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                when (method) {
                    "GET" -> b.get()
                    "POST" -> b.post((body?.toString() ?: "{}").toRequestBody(JSON))
                    "PATCH" -> b.patch((body?.toString() ?: "{}").toRequestBody(JSON))
                    else -> return@withContext VbResult.Err("unsupported method $method")
                }
                client.newCall(b.build()).execute().use { r ->
                    val text = r.body?.string() ?: ""
                    if (r.isSuccessful) VbResult.Ok(Raw(r.code, text))
                    else VbResult.Err(httpError(r.code, text), r.code)
                }
            } catch (e: Exception) {
                VbResult.Err("Network failure: ${e.message ?: e.javaClass.simpleName}")
            }
        }

    suspend fun validatePat(pat: String): VbResult<String> =
        raw(pat, "GET", "/user", null).map { JSONObject(it.body).optString("login", "unknown") }

    suspend fun commitOps(
        pat: String,
        owner: String,
        repo: String,
        branch: String,
        message: String,
        ops: List<BridgeOp>
    ): VbResult<CommitInfo> {
        val ref = raw(pat, "GET", "/repos/$owner/$repo/git/ref/heads/$branch", null)
            .getOrNull() ?: return VbResult.Err(refError(owner, repo, branch))
        val refSha = JSONObject(ref.body).getJSONObject("object").getString("sha")
        val baseCommit = raw(pat, "GET", "/repos/$owner/$repo/git/commits/$refSha", null).getOrNull()
            ?: return VbResult.Err("Could not read base commit for branch $branch.")
        val baseTree = JSONObject(baseCommit.body).getJSONObject("tree").getString("sha")
        val entries = JSONArray()
        for (op in ops) {
            when (op) {
                is BridgeOp.FileOp -> {
                    val blob = postBlob(pat, owner, repo, op.content.toByteArray(Charsets.UTF_8)).getOrNull()
                        ?: return VbResult.Err("Blob upload failed for ${op.path}.")
                    entries.put(JSONObject().apply {
                        put("path", op.path); put("mode", "100644"); put("type", "blob"); put("sha", blob)
                    })
                }
                is BridgeOp.EditOp -> {
                    val cur = fetchContent(pat, owner, repo, branch, op.path).getOrNull()
                        ?: return VbResult.Err("Cannot read ${op.path} from $branch for editing.")
                    var content = cur
                    for (h in op.hunks) {
                        val next = BridgeParser.applyEdit(content, h.find, h.replace)
                            ?: return VbResult.Err("Hunk has no match in ${op.path}: ${BridgeParser.snippet(h.find)}")
                        content = next
                    }
                    val blob = postBlob(pat, owner, repo, content.toByteArray(Charsets.UTF_8)).getOrNull()
                        ?: return VbResult.Err("Blob upload failed for ${op.path}.")
                    entries.put(JSONObject().apply {
                        put("path", op.path); put("mode", "100644"); put("type", "blob"); put("sha", blob)
                    })
                }
                is BridgeOp.DeleteOp -> {
                    entries.put(JSONObject().apply {
                        put("path", op.path); put("mode", "100644"); put("type", "blob"); put("sha", JSONObject.NULL)
                    })
                }
            }
        }
        if (entries.length() == 0) return VbResult.Err("Nothing to commit.")
        val treeBody = JSONObject().apply { put("base_tree", baseTree); put("tree", entries) }
        val tree = raw(pat, "POST", "/repos/$owner/$repo/git/trees", treeBody).getOrNull()
            ?: return VbResult.Err("Tree creation failed.")
        val treeSha = JSONObject(tree.body).getString("sha")
        val commitBody = JSONObject().apply {
            put("message", message)
            put("tree", treeSha)
            put("parents", JSONArray().put(refSha))
        }
        val commit = raw(pat, "POST", "/repos/$owner/$repo/git/commits", commitBody).getOrNull()
            ?: return VbResult.Err("Commit creation failed.")
        val commitObj = JSONObject(commit.body)
        val commitSha = commitObj.getString("sha")
        val patch = raw(pat, "PATCH", "/repos/$owner/$repo/git/refs/heads/$branch", JSONObject().put("sha", commitSha))
        if (patch is VbResult.Err) return VbResult.Err("Ref update failed: ${patch.message}")
        return VbResult.Ok(CommitInfo(commitSha, commitObj.optString("html_url", "")))
    }

    private fun refError(owner: String, repo: String, branch: String): String =
        "Cannot resolve refs/heads/$branch on $owner/$repo. Verify repository name, branch, and token scope."

    private suspend fun postBlob(pat: String, owner: String, repo: String, bytes: ByteArray): VbResult<String> {
        val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val body = JSONObject().apply { put("content", b64); put("encoding", "base64") }
        return raw(pat, "POST", "/repos/$owner/$repo/git/blobs", body)
            .map { JSONObject(it.body).getString("sha") }
    }

    private suspend fun fetchContent(pat: String, owner: String, repo: String, branch: String, path: String): VbResult<String> =
        raw(pat, "GET", "/repos/$owner/$repo/contents/${enc(path)}?ref=$branch", null).map {
            val content = JSONObject(it.body).optString("content", "")
            String(android.util.Base64.decode(content, android.util.Base64.DEFAULT), Charsets.UTF_8)
        }

    suspend fun runs(pat: String, owner: String, repo: String, branch: String): VbResult<List<RunInfo>> =
        raw(pat, "GET", "/repos/$owner/$repo/actions/runs?branch=$branch&per_page=5", null).map {
            val out = mutableListOf<RunInfo>()
            val a = JSONObject(it.body).optJSONArray("workflow_runs") ?: JSONArray()
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                out += RunInfo(
                    o.optLong("id"), o.optString("name"), o.optString("status"),
                    o.optString("conclusion"), o.optString("html_url"), o.optString("created_at")
                )
            }
            out
        }

    suspend fun artifacts(pat: String, owner: String, repo: String, runId: Long): VbResult<List<ArtifactInfo>> =
        raw(pat, "GET", "/repos/$owner/$repo/actions/runs/$runId/artifacts", null).map {
            val out = mutableListOf<ArtifactInfo>()
            val a = JSONObject(it.body).optJSONArray("artifacts") ?: JSONArray()
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                out += ArtifactInfo(
                    o.optString("name"), o.optLong("size_in_bytes"), o.optString("archive_download_url")
                )
            }
            out
        }

    suspend fun downloadArtifact(pat: String, url: String, outFile: File): VbResult<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $pat")
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext VbResult.Err("Download failed: HTTP ${response.code}")
                outFile.parentFile?.mkdirs()
                response.body?.byteStream()?.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext VbResult.Err("Empty response body")
                VbResult.Ok(outFile)
            }
        } catch (e: Exception) {
            VbResult.Err("Network failure: ${e.message}")
        }
    }
}
