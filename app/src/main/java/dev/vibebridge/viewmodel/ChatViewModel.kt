package dev.vibebridge.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.vibebridge.core.BridgeOp
import dev.vibebridge.core.BridgeParser
import dev.vibebridge.core.ChatMsg
import dev.vibebridge.core.GitHubClient
import dev.vibebridge.core.HistoryStore
import dev.vibebridge.core.NoteKind
import dev.vibebridge.core.NoteMsg
import dev.vibebridge.core.ParseMsg
import dev.vibebridge.core.PlanRow
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.PromptMsg
import dev.vibebridge.core.PromptTemplates
import dev.vibebridge.core.PushMsg
import dev.vibebridge.core.PushState
import dev.vibebridge.core.SecureStore
import dev.vibebridge.core.UserIdea
import dev.vibebridge.core.UserPayload
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.core.VbResult
import dev.vibebridge.core.Workspace
import java.io.File
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val secure = SecureStore(app)
    private val history = HistoryStore(app)
    private val github = GitHubClient()
    private val workspace = Workspace(File(app.filesDir, "workspace"))

    data class Ui(
        val messages: List<ChatMsg> = emptyList(),
        val input: String = "",
        val thinking: Boolean = false,
        val clipHint: String? = null,
        val lastClip: String = ""
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    private var seq = 0L
    private fun nextId() = ++seq
    private var pendingOps: List<BridgeOp>? = null
    private var clipJob: Job? = null
    private var logCache: Pair<Long, String>? = null

    private fun append(m: ChatMsg) = _ui.update { it.copy(messages = it.messages + m) }

    private fun updatePush(id: Long, f: (PushMsg) -> PushMsg) = _ui.update {
        it.copy(messages = it.messages.map { m -> if (m.id == id && m is PushMsg) f(m) else m })
    }

    fun setInput(s: String) = _ui.update { it.copy(input = s) }

    fun send() {
        val text = _ui.value.input.trim()
        if (text.isEmpty()) return
        val looksLikePayload = text.contains("===VIBEBRIDGE===") ||
            text.contains("===== FILE:") ||
            text.contains("===== EDIT:") ||
            text.contains("===== DELETE:")
        if (looksLikePayload) {
            submitPayload(text)
        } else {
            sendIdea(text)
        }
    }

    fun sendIdea(text: String) {
        append(UserIdea(nextId(), text))
        _ui.update { it.copy(input = "", thinking = true) }
        viewModelScope.launch {
            delay(350)
            append(PromptMsg(nextId(), PromptTemplates.compile(text, prefs.target), prefs.target))
            _ui.update { it.copy(thinking = false) }
        }
    }

    private fun previewFor(op: BridgeOp): List<String> {
        val lines = when (op) {
            is BridgeOp.FileOp -> {
                val all = op.content.lines()
                all.take(8).map { "+ $it" } +
                    if (all.size > 8) listOf("+ … (${all.size - 8} more lines)") else emptyList()
            }
            is BridgeOp.EditOp -> {
                op.hunks.take(2).flatMap { h ->
                    h.find.lines().take(3).map { "- $it" } +
                        h.replace.lines().take(3).map { "+ $it" }
                } + if (op.hunks.size > 2) listOf("… ${op.hunks.size - 2} more hunks") else emptyList()
            }
            is BridgeOp.DeleteOp -> listOf("- (entire file removed)")
        }
        return lines.take(14)
    }

    fun submitPayload(text: String) {
        append(UserPayload(nextId(), text))
        _ui.update { it.copy(input = "", thinking = true) }
        viewModelScope.launch {
            delay(300)
            val r = BridgeParser.parse(text, prefs.strict)
            if (r.ops.isEmpty()) {
                val extra = r.warnings.firstOrNull()?.let { " — $it" } ?: ""
                append(NoteMsg(nextId(), "no bridge operations found in that paste$extra", NoteKind.ERROR))
                _ui.update { it.copy(thinking = false) }
            } else {
                pendingOps = r.ops
                val rows = r.ops.map { op ->
                    when (op) {
                        is BridgeOp.FileOp -> PlanRow(op.path, "CREATE", "${op.content.lines().size} lines", previewFor(op))
                        is BridgeOp.EditOp -> PlanRow(op.path, "EDIT", "${op.hunks.size} hunks", previewFor(op))
                        is BridgeOp.DeleteOp -> PlanRow(op.path, "DELETE", "remove file", previewFor(op))
                    }
                }
                append(ParseMsg(nextId(), rows, r.warnings, r.ops.size))
                _ui.update { it.copy(thinking = false) }
            }
        }
    }

    fun applyLocal() {
        val ops = pendingOps
        if (ops == null) {
            append(NoteMsg(nextId(), "nothing staged to apply", NoteKind.WARN))
            return
        }
        val rep = workspace.applyOps(ops)
        val tail = if (rep.errors.isEmpty()) "" else " • ${rep.errors.first()}"
        append(
            NoteMsg(
                nextId(),
                "sandbox updated — created ${rep.created}, overwritten ${rep.overwritten}, edited ${rep.edited}, deleted ${rep.deleted}$tail",
                if (rep.errors.isEmpty()) NoteKind.INFO else NoteKind.WARN
            )
        )
    }

    fun push() {
        val ops = pendingOps
        if (ops == null) {
            append(NoteMsg(nextId(), "nothing staged to push", NoteKind.WARN))
            return
        }
        val (owner, repo) = prefs.splitRepo()
        if (owner.isBlank() || secure.pat.isBlank()) {
            append(NoteMsg(nextId(), "not connected to GitHub — open settings and connect first", NoteKind.ERROR))
            return
        }
        val id = nextId()
        append(PushMsg(id, PushState.PREPARING, null, null, null, null, null))
        viewModelScope.launch {
            updatePush(id) { it.copy(state = PushState.COMMITTING, note = "uploading blobs and tree") }
            val msg = "feat: vibebridge chat push (${ops.size} ops)"
            when (val r = github.commitOps(secure.pat, owner, repo, prefs.branch, msg, ops)) {
                is VbResult.Err -> {
                    updatePush(id) { it.copy(state = PushState.FAILED, note = r.message) }
                }
                is VbResult.Ok -> {
                    val recId = history.addPush(prefs.repo, prefs.branch, msg, ops.size, r.value.sha).id
                    prefs.lastCommitSha = r.value.sha
                    pendingOps = null
                    updatePush(id) { it.copy(state = PushState.POLLING, sha = r.value.sha, runUrl = r.value.url, note = "waiting for workflow run") }
                    var attempts = 0
                    while (attempts < 24) {
                        delay(8000)
                        attempts++
                        val runsResult = github.runs(secure.pat, owner, repo, prefs.branch)
                        val runs = (runsResult as? VbResult.Ok)?.value ?: continue
                        val active = runs.firstOrNull { it.status != "completed" }
                        if (active != null) {
                            val log = fetchLog(active.id, force = true)
                            val tail = if (log != null) tailLines(log, 3) else null
                            updatePush(id) {
                                it.copy(
                                    note = "run ${active.status}: ${active.name}",
                                    logTail = tail
                                )
                            }
                            continue
                        }
                        val done = runs.firstOrNull() ?: continue
                        history.setCi(recId, done.conclusion)
                        updatePush(id) {
                            it.copy(
                                state = PushState.DONE,
                                conclusion = done.conclusion,
                                note = "run finished",
                                runId = done.id,
                                logTail = null
                            )
                        }
                        return@launch
                    }
                    updatePush(id) { it.copy(state = PushState.DONE, conclusion = "unknown", note = "poll window ended", logTail = null) }
                }
            }
        }
    }

    private fun tailLines(log: String, n: Int): List<String> =
        log.lines().filter { it.isNotBlank() }.takeLast(n)

    private suspend fun fetchLog(runId: Long, force: Boolean = false): String? {
        if (!force) {
            logCache?.let { if (it.first == runId) return it.second }
        }
        val (owner, repo) = prefs.splitRepo()
        val jobsResult = github.jobs(secure.pat, owner, repo, runId)
        val jobs = (jobsResult as? VbResult.Ok)?.value
        if (jobs.isNullOrEmpty()) return null
        val failed = jobs.firstOrNull { it.conclusion == "failure" } ?: jobs.first()
        val logResult = github.jobLog(secure.pat, owner, repo, failed.id)
        val log = (logResult as? VbResult.Ok)?.value
        if (log.isNullOrBlank()) return null
        logCache = runId to log
        return log
    }

    private fun extractErrors(log: String): List<String> {
        val out = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        val ePattern = Regex("e: file://[^\\n]*")
        for (m in ePattern.findAll(log)) {
            val v = m.value.trim()
            if (seen.add(v)) out.add(v)
        }
        if (out.isEmpty()) {
            for (line in log.lines()) {
                val idx = line.indexOf("error:")
                if (idx >= 0) {
                    val v = line.substring(idx).trim()
                    if (seen.add(v)) out.add(v)
                }
            }
        }
        if (out.isEmpty()) {
            val w = log.indexOf("What went wrong:")
            if (w >= 0) {
                out.add(log.substring(w, (w + 800).coerceAtMost(log.length)).trim())
            }
        }
        return out
    }

    fun fixIt(runId: Long?, fallback: String? = null) {
        viewModelScope.launch {
            val errs = if (runId != null) {
                val log = fetchLog(runId)
                if (log == null) {
                    listOfNotNull(fallback).filter { it.isNotBlank() }
                } else {
                    extractErrors(log).ifEmpty { log.lines().filter { it.isNotBlank() }.takeLast(30) }
                }
            } else {
                listOfNotNull(fallback).filter { it.isNotBlank() }
            }
            if (errs.isEmpty()) {
                append(NoteMsg(nextId(), "no error text found to build a fix prompt", NoteKind.WARN))
                return@launch
            }
            val idea = buildString {
                appendLine("My Android Kotlin app CI build failed with these compiler/gradle errors:")
                appendLine("```")
                errs.take(25).forEach { appendLine(it) }
                appendLine("```")
                appendLine("Fix every error above in the exact files mentioned. Reply with a bridge payload only.")
            }
            append(PromptMsg(nextId(), PromptTemplates.compile(idea, prefs.target), prefs.target))
            append(NoteMsg(nextId(), "fix prompt compiled from ${errs.size} error lines — copy it into your AI chat", NoteKind.INFO))
        }
    }

    fun copyErrors(runId: Long) {
        viewModelScope.launch {
            val log = fetchLog(runId) ?: return@launch
            val errs = extractErrors(log)
            val text = if (errs.isEmpty()) log.takeLast(20000) else errs.joinToString("\n")
            val ok = VbClipboard.copy(getApplication(), "ci-errors", text)
            append(
                NoteMsg(
                    nextId(),
                    if (ok) "copied ${if (errs.isEmpty()) "log tail" else "${errs.size} error lines"} to clipboard" else "clipboard unavailable",
                    if (ok) NoteKind.INFO else NoteKind.ERROR
                )
            )
        }
    }

    fun saveErrorsMd(runId: Long, sha: String?) {
        viewModelScope.launch {
            val log = fetchLog(runId) ?: return@launch
            val errs = extractErrors(log)
            val md = buildString {
                appendLine("# VibeBridge CI failure")
                appendLine()
                appendLine("- commit: ${sha ?: "unknown"}")
                appendLine("- run id: $runId")
                appendLine("- fetched: ${java.util.Date()}")
                appendLine()
                appendLine("## Extracted errors (${errs.size})")
                appendLine("```text")
                errs.forEach { appendLine(it) }
                appendLine("```")
                appendLine()
                appendLine("## Log head (first 4k chars)")
                appendLine("```text")
                appendLine(log.take(4000))
                appendLine("```")
                appendLine()
                appendLine("## Full log (tail 20k chars)")
                appendLine("```text")
                appendLine(log.takeLast(20000))
                appendLine("```")
            }
            val name = "vibebridge-errors-${(sha ?: runId.toString()).take(7)}.md"
            val saved = saveToDownloads(name, md.toByteArray(Charsets.UTF_8), "text/markdown")
            append(
                NoteMsg(
                    nextId(),
                    if (saved) "saved $name to Downloads" else "could not save file",
                    if (saved) NoteKind.INFO else NoteKind.ERROR
                )
            )
        }
    }

    private suspend fun fetchApk(runId: Long): ByteArray? {
        val (owner, repo) = prefs.splitRepo()
        val artsResult = github.artifacts(secure.pat, owner, repo, runId)
        val arts = (artsResult as? VbResult.Ok)?.value
        if (arts.isNullOrEmpty()) {
            append(NoteMsg(nextId(), "no artifacts found for this run", NoteKind.WARN))
            return null
        }
        val zipFile = File(getApplication<Application>().filesDir, "out/artifact-$runId.zip")
        val dlResult = github.downloadArtifact(secure.pat, arts.first().downloadUrl, zipFile)
        if (dlResult is VbResult.Err) {
            append(NoteMsg(nextId(), "download failed: ${dlResult.message}", NoteKind.ERROR))
            return null
        }
        val apkDir = File(getApplication<Application>().filesDir, "out/apk-$runId")
        val apkFile = extractApk(zipFile, apkDir)
        if (apkFile == null) {
            append(NoteMsg(nextId(), "no apk found in artifact zip", NoteKind.WARN))
            return null
        }
        return apkFile.readBytes()
    }

    fun saveApkToDownloads(runId: Long, sha: String?) {
        val (owner, repo) = prefs.splitRepo()
        if (owner.isBlank() || secure.pat.isBlank()) return
        append(NoteMsg(nextId(), "fetching artifact...", NoteKind.INFO))
        viewModelScope.launch {
            val bytes = fetchApk(runId) ?: return@launch
            val name = "vibebridge-${(sha ?: runId.toString()).take(7)}.apk"
            val saved = saveToDownloads(name, bytes, "application/vnd.android.package-archive")
            append(
                NoteMsg(
                    nextId(),
                    if (saved) "saved $name to Downloads folder" else "could not save apk",
                    if (saved) NoteKind.INFO else NoteKind.ERROR
                )
            )
        }
    }

    fun shareApk(runId: Long) {
        val (owner, repo) = prefs.splitRepo()
        if (owner.isBlank() || secure.pat.isBlank()) return
        append(NoteMsg(nextId(), "fetching artifact...", NoteKind.INFO))
        viewModelScope.launch {
            val bytes = fetchApk(runId) ?: return@launch
            val apkFile = File(getApplication<Application>().filesDir, "out/share-apk/app-debug.apk")
            apkFile.parentFile?.mkdirs()
            apkFile.writeBytes(bytes)
            val ctx = getApplication<Application>()
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".vbfiles", apkFile)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(Intent.createChooser(share, "Share APK").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun installApk(runId: Long, sha: String?) {
        val (owner, repo) = prefs.splitRepo()
        if (owner.isBlank() || secure.pat.isBlank()) return
        append(NoteMsg(nextId(), "fetching artifact for install...", NoteKind.INFO))
        viewModelScope.launch {
            val bytes = fetchApk(runId) ?: return@launch
            val ctx = getApplication<Application>()
            val apkFile = File(ctx.filesDir, "out/install/vibebridge-install.apk")
            apkFile.parentFile?.mkdirs()
            apkFile.writeBytes(bytes)
            if (!ctx.packageManager.canRequestPackageInstalls()) {
                append(
                    NoteMsg(
                        nextId(),
                        "one-time: allow 'install unknown apps' for vibebridge, then tap install again",
                        NoteKind.WARN
                    )
                )
                runCatching {
                    ctx.startActivity(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                return@launch
            }
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".vbfiles", apkFile)
            val install = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { ctx.startActivity(install) }
                .onFailure { append(NoteMsg(nextId(), "installer failed: ${it.message}", NoteKind.ERROR)) }
        }
    }

    private fun saveToDownloads(name: String, bytes: ByteArray, mime: String): Boolean {
        val ctx = getApplication<Application>()
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false
                true
            } else {
                val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
                dir.mkdirs()
                File(dir, name).writeBytes(bytes)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun extractApk(zipFile: File, outDir: File): File? {
        outDir.mkdirs()
        var apkFile: File? = null
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".apk")) {
                    val outFile = File(outDir, entry.name.substringAfterLast('/'))
                    outFile.outputStream().use { zis.copyTo(it) }
                    apkFile = outFile
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return apkFile
    }

    fun templateOptions(): List<Pair<String, String>> =
        history.templates().map { it.name to it.idea } + PromptTemplates.BUILTINS.map { it.name to it.idea }

    fun sandboxFiles(): List<String> = workspace.tree()

    fun attachFile(path: String) {
        val content = workspace.read(path)
        if (content == null) {
            append(NoteMsg(nextId(), "cannot read $path", NoteKind.WARN))
            return
        }
        val ok = VbClipboard.copy(getApplication(), "vibe-file", content)
        append(
            NoteMsg(
                nextId(),
                if (ok) "copied $path to clipboard — paste it into your AI chat" else "clipboard unavailable",
                if (ok) NoteKind.INFO else NoteKind.ERROR
            )
        )
    }

    fun currentStrict(): Boolean = prefs.strict

    fun toggleStrict(): Boolean {
        prefs.strict = !prefs.strict
        return prefs.strict
    }

    fun newChat() {
        pendingOps = null
        _ui.update { Ui() }
    }

    fun currentTarget(): String = prefs.target

    fun setTarget(t: String) { prefs.target = t }

    fun startClipWatch() {
        if (clipJob != null) return
        clipJob = viewModelScope.launch {
            while (isActive) {
                delay(1500)
                if (!prefs.autoClip) continue
                val t = VbClipboard.read(getApplication())
                if (t.isBlank() || t == _ui.value.lastClip) continue
                _ui.update { it.copy(lastClip = t) }
                val looksLike = t.contains("===VIBEBRIDGE===") ||
                    t.contains("===== FILE:") ||
                    t.contains("===== EDIT:") ||
                    t.contains("===== DELETE:")
                if (looksLike && _ui.value.clipHint == null) {
                    _ui.update { it.copy(clipHint = t) }
                }
            }
        }
    }

    fun clipIntoInput() {
        val t = _ui.value.clipHint ?: VbClipboard.read(getApplication())
        if (t.isNotBlank()) _ui.update { it.copy(input = t, clipHint = null) }
    }

    fun dismissClip() = _ui.update { it.copy(clipHint = null) }

    override fun onCleared() {
        clipJob?.cancel()
        super.onCleared()
    }
}
