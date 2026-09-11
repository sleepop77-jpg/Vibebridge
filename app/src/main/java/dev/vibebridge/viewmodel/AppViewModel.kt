package dev.vibebridge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.vibebridge.core.GitHubClient
import dev.vibebridge.core.HistoryStore
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.PushRecord
import dev.vibebridge.core.RunInfo
import dev.vibebridge.core.SecureStore
import dev.vibebridge.core.VbResult
import dev.vibebridge.core.Workspace
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val prefs = Prefs(app)
    val secure = SecureStore(app)
    val history = HistoryStore(app)
    val workspace = Workspace(File(app.filesDir, "workspace"))
    private val github = GitHubClient()

    data class Ui(
        val onboarded: Boolean = false,
        val validating: Boolean = false,
        val validateError: String? = null,
        val login: String? = null,
        val files: Int = 0,
        val bytes: Long = 0L,
        val lastPush: PushRecord? = null,
        val lastRun: RunInfo? = null,
        val refreshing: Boolean = false,
        val refreshError: String? = null,
        val strict: Boolean = false,
        val mirrorBound: Boolean = false,
        val infoBanner: String? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    init { reloadLocal() }

    fun reloadLocal() {
        _ui.update {
            it.copy(
                onboarded = secure.pat.isNotBlank() && prefs.configured,
                files = workspace.tree().size,
                bytes = workspace.totalBytes(),
                lastPush = history.pushes().firstOrNull(),
                strict = prefs.strict,
                mirrorBound = prefs.mirrorUri.isNotBlank()
            )
        }
    }

    fun banner(msg: String?) = _ui.update { it.copy(infoBanner = msg) }
    fun setTarget(t: String) { prefs.target = t }
    fun setStrict(b: Boolean) {
        prefs.strict = b
        _ui.update { it.copy(strict = b) }
    }

    fun saveConnect(pat: String, repo: String, branch: String) {
        secure.pat = pat.trim()
        prefs.repo = repo.trim()
        prefs.branch = branch.trim().ifBlank { "main" }
        _ui.update { it.copy(validating = true, validateError = null) }
        viewModelScope.launch {
            when (val r = github.validatePat(secure.pat)) {
                is VbResult.Ok -> {
                    _ui.update { it.copy(validating = false, login = r.value, onboarded = true) }
                    reloadLocal()
                }
                is VbResult.Err -> _ui.update { it.copy(validating = false, validateError = r.message, onboarded = false) }
            }
        }
    }

    fun refresh() {
        val (owner, name) = prefs.splitRepo()
        if (owner.isBlank()) {
            _ui.update { it.copy(refreshError = "Repository not configured.") }
            return
        }
        _ui.update { it.copy(refreshing = true, refreshError = null) }
        viewModelScope.launch {
            when (val r = github.runs(secure.pat, owner, name, prefs.branch)) {
                is VbResult.Ok -> {
                    _ui.update { it.copy(refreshing = false, lastRun = r.value.firstOrNull()) }
                    reloadLocal()
                }
                is VbResult.Err -> _ui.update { it.copy(refreshing = false, refreshError = r.message) }
            }
        }
    }

    fun disconnect() {
        secure.clear()
        _ui.update { it.copy(onboarded = false, login = null) }
    }
}
