package dev.vibebridge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.vibebridge.core.GitRevert
import dev.vibebridge.core.GitHubClient
import dev.vibebridge.core.HistoryStore
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.PushRecord
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
        val login: String? = null,
        val validating: Boolean = false,
        val validateError: String? = null,
        val strict: Boolean = false,
        val files: Int = 0,
        val bytes: Long = 0L,
        val mirrorBound: Boolean = false,
        val banner: String? = null,
        val revertBusy: Boolean = false,
        val revertMsg: String? = null,
        val revertOk: Boolean = false
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _ui.update {
            it.copy(
                onboarded = prefs.configured && secure.pat.isNotBlank(),
                strict = prefs.strict,
                files = workspace.tree().size,
                bytes = workspace.totalBytes(),
                mirrorBound = prefs.mirrorUri.isNotBlank()
            )
        }
    }

    fun banner(msg: String) = _ui.update { it.copy(banner = msg) }
    fun clearBanner() = _ui.update { it.copy(banner = null) }

    fun setStrict(b: Boolean) {
        prefs.strict = b
        _ui.update { it.copy(strict = b) }
    }

    fun setTarget(t: String) {
        prefs.target = t
    }

    fun saveConnect(pat: String, repo: String, branch: String) {
        _ui.update { it.copy(validating = true, validateError = null) }
        viewModelScope.launch {
            when (val r = github.validatePat(pat.trim())) {
                is VbResult.Ok -> {
                    secure.pat = pat.trim()
                    prefs.repo = repo.trim()
                    prefs.branch = branch.trim().ifBlank { "main" }
                    _ui.update { it.copy(validating = false, login = r.value) }
                    refresh()
                }
                is VbResult.Err -> _ui.update { it.copy(validating = false, validateError = r.message) }
            }
        }
    }

    fun revertPush(rec: PushRecord) {
        val parts = rec.repo.split("/", limit = 2)
        if (parts.size != 2 || secure.pat.isBlank()) {
            _ui.update { it.copy(revertBusy = false, revertOk = false, revertMsg = "not connected — cannot rewind") }
            return
        }
        _ui.update { it.copy(revertBusy = true, revertMsg = null) }
        viewModelScope.launch {
            when (val parent = GitRevert.parentSha(secure.pat, parts[0], parts[1], rec.sha)) {
                is VbResult.Err -> {
                    _ui.update { it.copy(revertBusy = false, revertOk = false, revertMsg = parent.message) }
                }
                is VbResult.Ok -> {
                    when (val r = GitRevert.forceRef(secure.pat, parts[0], parts[1], rec.branch, parent.value)) {
                        is VbResult.Err -> {
                            _ui.update { it.copy(revertBusy = false, revertOk = false, revertMsg = r.message) }
                        }
                        is VbResult.Ok -> {
                            _ui.update {
                                it.copy(
                                    revertBusy = false,
                                    revertOk = true,
                                    revertMsg = "rewound ${rec.branch} to ${parent.value.take(7)} (before ${rec.sha.take(7)})"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun clearRevertMsg() = _ui.update { it.copy(revertMsg = null) }

    fun disconnect() {
        secure.pat = ""
        _ui.update { Ui() }
    }
}
