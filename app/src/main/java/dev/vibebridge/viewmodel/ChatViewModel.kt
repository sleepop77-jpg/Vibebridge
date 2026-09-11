package dev.vibebridge.viewmodel

import android.app.Application
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

    enum class Stage { IDEA, AWAIT }

    data class Ui(
        val messages: List<ChatMsg> = emptyList(),
        val input: String = "",
        val thinking: Boolean = false,
        val stage: Stage = Stage.IDEA,
        val clipHint: String? = null,
        val lastClip: String = ""
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    private var seq = 0L
    private fun nextId() = ++seq
    private var pendingOps: List<BridgeOp>? = null
    private var clipJob: Job? = null

    private fun append(m: ChatMsg) = _ui.update { it.copy(messages = it.messages + m) }

    private fun updatePush(id: Long, f: (PushMsg) -> PushMsg) = _ui.update {
        it.copy(messages = it.messages.map { m -> if (m.id == id && m is PushMsg) f(m) else m })
    }

    fun setInput(s: String) = _ui.update { it.copy(input = s) }

fun currentTarget(): String = prefs.target

fun setTarget(t: String) { prefs.target = t }

    fun send() {
        val text = _ui.value.input.trim()
        if (text.isEmpty()) return
        when (_ui.value.stage) {
            Stage.IDEA -> sendIdea(text)
            Stage.AWAIT -> submitPayload(text)
        }
    }

    fun sendIdea(text: String) {
        append(UserIdea(nextId(), text))
        _ui.update { it.copy(input = "", thinking = true) }
        viewModelScope.launch {
            delay(350)
            append(PromptMsg(nextId(), PromptTemplates.compile(text, prefs.target), prefs.target))
            _ui.update { it.copy(thinking = false, stage = Stage.AWAIT) }
        }
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
                        is BridgeOp.FileOp -> PlanRow(op.path, "CREATE", "${op.content.lines().size} lines")
                        is BridgeOp.EditOp -> PlanRow(op.path, "EDIT", "${op.hunks.size} hunks")
                        is BridgeOp.DeleteOp -> PlanRow(op.path, "DELETE", "remove file")
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
        append(PushMsg(id, PushState.PREPARING, null, null, null, null))
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
                            updatePush(id) { it.copy(note = "run ${active.status}: ${active.name}") }
                            continue
                        }
                        val done = runs.firstOrNull() ?: continue
                        history.setCi(recId, done.conclusion)
                        updatePush(id) { it.copy(state = PushState.DONE, conclusion = done.conclusion, note = "run finished") }
                        _ui.update { it.copy(stage = Stage.IDEA) }
                        return@launch
                    }
                    updatePush(id) { it.copy(state = PushState.DONE, conclusion = "unknown", note = "poll window ended") }
                    _ui.update { it.copy(stage = Stage.IDEA) }
                }
            }
        }
    }

    fun newChat() {
        pendingOps = null
        _ui.update { Ui() }
    }

    fun startClipWatch() {
        if (clipJob != null) return
        clipJob = viewModelScope.launch {
            while (isActive) {
                delay(1500)
                if (!prefs.autoClip) continue
                val t = VbClipboard.read(getApplication())
                if (t.isBlank() || t == _ui.value.lastClip) continue
                _ui.update { it.copy(lastClip = t) }
                val looksLike = t.contains("===VIBEBRIDGE===") || t.contains("===== FILE:")
                if (looksLike && _ui.value.clipHint == null && _ui.value.stage == Stage.AWAIT) {
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
