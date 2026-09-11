package dev.vibebridge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.vibebridge.core.GitHubClient
import dev.vibebridge.core.HistoryStore
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.SecureStore
import dev.vibebridge.core.VbResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PushViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val secure = SecureStore(app)
    private val history = HistoryStore(app)
    private val github = GitHubClient()

    enum class Phase { IDLE, COMMITTING, POLLING, DONE, FAILED }

    data class Ui(
        val message: String = "",
        val phase: Phase = Phase.IDLE,
        val status: String = "",
        val error: String? = null,
        val commitSha: String? = null,
        val conclusion: String? = null,
        val runUrl: String? = null,
        val artifactName: String? = null,
        val artifactBytes: Long = 0L
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()
    private var pollJob: Job? = null

    val canPush: Boolean
        get() = OpsHandoff.ops.isNotEmpty() && secure.pat.isNotBlank() && prefs.configured

    fun pushDisabledReason(): String = when {
        OpsHandoff.ops.isEmpty() -> "Stage operations in GRAB first."
        secure.pat.isBlank() -> "Connect a token in SETTINGS first."
        !prefs.configured -> "Configure repository in SETTINGS first."
        else -> ""
    }

    fun setMessage(m: String) = _ui.update { it.copy(message = m) }

    fun push() {
        if (!canPush) return
        val (owner, repo) = prefs.splitRepo()
        val msg = _ui.value.message.ifBlank { "chore: vibebridge operations" }
        _ui.update { it.copy(phase = Phase.COMMITTING, error = null, status = "uploading blobs and tree") }
        viewModelScope.launch {
            val r = github.commitOps(secure.pat, owner, repo, prefs.branch, msg, OpsHandoff.ops)
            when (r) {
                is VbResult.Err -> _ui.update { it.copy(phase = Phase.FAILED, error = r.message) }
                is VbResult.Ok -> {
                    val id = history.addPush(prefs.repo, prefs.branch, msg, OpsHandoff.ops.size, r.value.sha).id
                    prefs.lastCommitSha = r.value.sha
                    _ui.update {
                        it.copy(
                            phase = Phase.POLLING,
                            commitSha = r.value.sha,
                            runUrl = r.value.url,
                            status = "waiting for workflow run"
                        )
                    }
                    pollJob?.cancel()
                    pollJob = viewModelScope.launch {
                        var attempts = 0
                        while (attempts < 30) {
                            delay(8000)
                            attempts++
                            val runs = github.runs(secure.pat, owner, repo, prefs.branch).getOrNull() ?: continue
                            val active = runs.firstOrNull { it.status != "completed" }
                            if (active != null) {
                                _ui.update { it.copy(status = "run ${active.status}: ${active.name}") }
                                continue
                            }
                            val done = runs.firstOrNull()
                            if (done == null) continue
                            history.setCi(id, done.conclusion)
                            val arts = github.artifacts(secure.pat, owner, repo, done.id).getOrNull().orEmpty()
                            val first = arts.firstOrNull()
                            _ui.update {
                                it.copy(
                                    phase = Phase.DONE,
                                    conclusion = done.conclusion,
                                    runUrl = done.url,
                                    artifactName = first?.name,
                                    artifactBytes = first?.bytes ?: 0L,
                                    status = "run finished"
                                )
                            }
                            return@launch
                        }
                        _ui.update {
                            it.copy(phase = Phase.DONE, conclusion = "unknown", status = "poll window ended")
                        }
                    }
                }
            }
        }
    }

    fun reset() {
        pollJob?.cancel()
        _ui.update { Ui(message = it.message) }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
