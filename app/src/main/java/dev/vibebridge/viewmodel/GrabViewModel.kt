package dev.vibebridge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.vibebridge.core.ApplyReport
import dev.vibebridge.core.BridgeOp
import dev.vibebridge.core.BridgeParser
import dev.vibebridge.core.OpReport
import dev.vibebridge.core.ParseResult
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.core.Workspace
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object OpsHandoff {
    var ops: List<BridgeOp> = emptyList()
        private set
    var label: String = ""
        private set

    fun set(ops: List<BridgeOp>, label: String) {
        this.ops = ops
        this.label = label
    }

    fun clear() {
        ops = emptyList()
        label = ""
    }
}

class GrabViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val workspace = Workspace(File(app.filesDir, "workspace"))

    val strict: Boolean get() = prefs.strict

    data class Ui(
        val pasted: String = "",
        val parsed: ParseResult? = null,
        val plan: List<OpReport> = emptyList(),
        val applied: ApplyReport? = null,
        val banner: String? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun setPasted(s: String) = _ui.update {
        it.copy(pasted = s, parsed = null, plan = emptyList(), applied = null, banner = null)
    }

    fun readClipboard() {
        setPasted(VbClipboard.read(getApplication()))
        OpsHandoff.clear()
    }

    fun parse() {
        val r = BridgeParser.parse(_ui.value.pasted, prefs.strict)
        OpsHandoff.set(r.ops, r.sentinel ?: "payload without sentinel")
        _ui.update {
            it.copy(
                parsed = r,
                plan = workspace.plan(r.ops),
                applied = null,
                banner = if (r.ops.isEmpty()) null else "${r.ops.size} operations staged"
            )
        }
    }

    fun apply() {
        val p = _ui.value.parsed ?: return
        _ui.update { it.copy(applied = workspace.applyOps(p.ops)) }
    }
}
