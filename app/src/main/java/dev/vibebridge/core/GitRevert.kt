package dev.vibebridge.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GitRevert {
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun call(pat: String, method: String, url: String, body: JSONObject?): VbResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val b = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $pat")
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                if (method == "GET") b.get() else b.patch((body?.toString() ?: "{}").toRequestBody(JSON))
                client.newCall(b.build()).execute().use { r ->
                    val text = r.body?.string() ?: ""
                    if (r.isSuccessful) VbResult.Ok(text) else VbResult.Err(GitHubClient.httpError(r.code, text), r.code)
                }
            } catch (e: Exception) {
                VbResult.Err("Network failure: ${e.message ?: e.javaClass.simpleName}")
            }
        }

    suspend fun parentSha(pat: String, owner: String, repo: String, sha: String): VbResult<String> {
        val r = call(pat, "GET", "https://api.github.com/repos/$owner/$repo/git/commits/$sha", null).getOrNull()
            ?: return VbResult.Err("cannot read commit ${sha.take(7)}")
        val parents = JSONObject(r).optJSONArray("parents")
        if (parents == null || parents.length() == 0) {
            return VbResult.Err("that commit has no parent — nothing to rewind to")
        }
        return VbResult.Ok(parents.getJSONObject(0).getString("sha"))
    }

    suspend fun forceRef(pat: String, owner: String, repo: String, branch: String, sha: String): VbResult<String> =
        call(
            pat,
            "PATCH",
            "https://api.github.com/repos/$owner/$repo/git/refs/heads/$branch",
            JSONObject().apply {
                put("sha", sha)
                put("force", true)
            }
        ).map { "ok" }
}
