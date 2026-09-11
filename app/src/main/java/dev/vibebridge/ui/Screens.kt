package dev.vibebridge.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.BridgeOp
import dev.vibebridge.core.ParseResult
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.Workspace
import dev.vibebridge.ui.theme.VbAmber
import dev.vibebridge.ui.theme.VbBg
import dev.vibebridge.ui.theme.VbBorder
import dev.vibebridge.ui.theme.VbCard
import dev.vibebridge.ui.theme.VbDim
import dev.vibebridge.ui.theme.VbGreen
import dev.vibebridge.ui.theme.VbRed
import dev.vibebridge.ui.theme.VbYellow

fun copyToClipboard(ctx: Context, label: String, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun readClipboard(ctx: Context): String {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString() ?: ""
}

@Composable
fun VbCard(title: String? = null, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VbCard, RoundedCornerShape(8.dp))
            .border(1.dp, VbBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (title != null) {
            Text(title, color = VbAmber, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
        }
        content()
    }
}

@Composable
private fun vbField() = TextFieldDefaults.colors(
    focusedContainerColor = VbBg,
    unfocusedContainerColor = VbBg,
    focusedIndicatorColor = VbAmber,
    unfocusedIndicatorColor = VbBorder,
    focusedTextColor = VbYellow,
    unfocusedTextColor = VbYellow,
    cursorColor = VbAmber
)

// ---------------- HOME ----------------
@Composable
fun HomeScreen(workspace: Workspace, prefs: Prefs, goto: (String) -> Unit) {
    val files = remember { workspace.tree().size }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("VIBEBRIDGE // SESSION", color = VbYellow, fontSize = 16.sp, fontWeight = FontWeight.BLACK)
        VbCard("WORKSPACE") {
            Text("${prefs.repo.ifBlank { "no repo yet" }} • sandbox $files files", color = VbYellow, fontSize = 13.sp)
        }
        VbCard("LAST PUSH") {
            Text("none yet — Package 2 wires GitHub", color = VbDim, fontSize = 12.sp)
        }
        VbCard("CI") {
            Text("— awaiting first push", color = VbDim, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { goto("COMPILER") }, modifier = Modifier.weight(1f)) { Text("COMPILE PROMPT") }
            Button(onClick = { goto("GRAB") }, modifier = Modifier.weight(1f)) { Text("GRAB CODE") }
        }
    }
}

// ---------------- COMPILER ----------------
fun compilePrompt(idea: String, target: String): String = buildString {
    appendLine("===VIBEBRIDGE=== v1 target=android")
    appendLine("You are a senior Android engineer working in a single-module Kotlin+Compose app (minSdk 24).")
    appendLine("Respond ONLY with bridge-format operations, no prose outside blocks:")
    appendLine("- whole file:  ===== FILE: path =====")
    appendLine("- surgical:    ===== EDIT: path ===== with --- FIND / --- REPLACE / --- END pairs")
    appendLine("- removal:     ===== DELETE: path =====")
    appendLine("Keep FIND blocks copy-exact from code you were shown. Prefer EDIT over FILE for small changes.")
    appendLine("Target notes: " + when (target) {
        "QWEN STUDIO" -> "output complete blocks; never truncate; split long work into multiple FILE blocks."
        "CHATGPT" -> "do not wrap blocks in markdown fences; paths relative to repo root."
        else -> "keep each block under ~300 lines; split larger files into EDIT hunks."
    })
    appendLine()
    appendLine("TASK:")
    appendLine(idea.trim())
}

@Composable
fun CompilerScreen() {
    val ctx = LocalContext.current
    var idea by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("QWEN STUDIO") }
    val compiled = remember(idea, target) { if (idea.isBlank()) "" else compilePrompt(idea, target) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("PROMPT COMPILER", color = VbYellow, fontSize = 16.sp, fontWeight = FontWeight.BLACK)
        VbCard("RAW IDEA") {
            TextField(
                value = idea,
                onValueChange = { idea = it },
                colors = vbField(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("QWEN STUDIO", "CHATGPT", "GEMINI").forEach { t ->
                val sel = t == target
                Box(
                    modifier = Modifier
                        .background(if (sel) VbAmber else VbCard, RoundedCornerShape(6.dp))
                        .border(1.dp, if (sel) VbAmber else VbBorder, RoundedCornerShape(6.dp))
                        .clickable { target = t }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) { Text(t, color = if (sel) Color.Black else VbDim, fontSize = 11.sp, fontWeight = FontWeight.BOLD) }
            }
        }
        VbCard("COMPILED PROMPT") {
            Text(
                compiled.ifBlank { "type an idea above — the contract + sentinel get injected automatically" },
                color = if (compiled.isBlank()) VbDim else VbGreen,
                fontSize = 11.sp
            )
        }
        Button(
            onClick = { copyToClipboard(ctx, "vibe-prompt", compiled) },
            enabled = compiled.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("COPY PROMPT", fontWeight = FontWeight.BLACK) }
    }
}

// ---------------- GRAB ----------------
@Composable
fun GrabScreen(workspace: Workspace) {
    val ctx = LocalContext.current
    var pasted by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<ParseResult?>(null) }
    var dry by remember { mutableStateOf<List<String>>(emptyList()) }
    var msg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("GRAB BOX", color = VbYellow, fontSize = 16.sp, fontWeight = FontWeight.BLACK)
        OutlinedButton(
            onClick = { pasted = readClipboard(ctx) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("PASTE FROM CLIPBOARD") }
        TextField(
            value = pasted,
            onValueChange = { pasted = it; parsed = null; dry = emptyList() },
            colors = vbField(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            label = { Text("or paste AI output here", color = VbDim, fontSize = 11.sp) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val r = dev.vibebridge.core.BridgeParser.parse(pasted)
                    parsed = r
                    dry = workspace.dryRun(r.ops)
                },
                enabled = pasted.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text("PARSE & PREVIEW") }
            Button(
                onClick = {
                    val r = parsed ?: return@Button
                    val rep = workspace.applyOps(r.ops)
                    msg = "created ${rep.created} • edited ${rep.edited} • deleted ${rep.deleted}" +
                        if (rep.errors.isEmpty()) "" else "\n" + rep.errors.joinToString("\n")
                },
                enabled = parsed != null,
                modifier = Modifier.weight(1f)
            ) { Text("APPLY TO WORKSPACE") }
        }
        parsed?.let { r ->
            VbCard("SENTINEL: " + (r.sentinel ?: "none found")) {
                r.ops.forEach { op ->
                    val (icon, color) = when (op) {
                        is BridgeOp.FileOp -> "+ " to VbGreen
                        is BridgeOp.EditOp -> "* " to VbAmber
                        is BridgeOp.DeleteOp -> "- " to VbRed
                    }
                    Text(icon + op.path, color = color, fontSize = 12.sp)
                }
                r.warnings.forEach { Text("! $it", color = VbRed, fontSize = 11.sp) }
            }
        }
        if (dry.isNotEmpty()) {
            VbCard("DRY RUN") {
                dry.forEach { line ->
                    val color = when {
                        line.startsWith("!") -> VbRed
                        line.startsWith("+") -> VbGreen
                        else -> VbYellow
                    }
                    Text(line, color = color, fontSize = 11.sp)
                }
            }
        }
        if (msg.isNotBlank()) {
            VbCard("APPLIED") { Text(msg, color = VbGreen, fontSize = 12.sp) }
        }
    }
}

// ---------------- WORKSPACE ----------------
@Composable
fun WorkspaceScreen(workspace: Workspace) {
    var refresh by remember { mutableStateOf(0) }
    val tree = remember(refresh) { workspace.tree() }
    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("WORKSPACE", color = VbYellow, fontSize = 16.sp, fontWeight = FontWeight.BLACK)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.background(VbGreen, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("SANDBOX ✓", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.BOLD)
            }
            Box(Modifier.background(VbCard, RoundedCornerShape(6.dp)).border(1.dp, VbBorder, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("SAF FOLDER: PKG 3", color = VbDim, fontSize = 11.sp)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .background(VbCard, RoundedCornerShape(8.dp))
                .border(1.dp, VbBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            if (tree.isEmpty()) item { Text("empty — apply a grab first", color = VbDim, fontSize = 12.sp) }
            items(tree) { p -> Text("· $p", color = VbYellow, fontSize = 11.sp) }
        }
        Text("sandbox ${tree.size} files", color = VbDim, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { refresh++ }, modifier = Modifier.weight(1f)) { Text("REFRESH") }
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) { Text("PULL (PKG 2)") }
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) { Text("ZIP (PKG 3)") }
        }
    }
}

// ---------------- LIBRARY ----------------
@Composable
fun LibraryScreen() {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("LIBRARY", color = VbYellow, fontSize = 16.sp, fontWeight = FontWeight.BLACK)
        listOf(
            "Settings screen pack" to "add a settings screen with PAT, repo and branch fields plus strict-parser toggle",
            "GitHub push pack" to "add a GitHubClient that commits workspace ops via the Git Data API and reports push status",
            "CI status pack" to "add CI run polling for the last commit and show success/failure with an artifact download link"
        ).forEach { (name, idea) ->
            VbCard(name) {
                Text(idea, color = VbDim, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { copyToClipboard(ctx, "vibe-template", compilePrompt(idea, "QWEN STUDIO")) }) {
                    Text("COPY AS PROMPT")
                }
            }
        }
        VbCard("HISTORY") { Text("no pushes yet — Package 2", color = VbDim, fontSize = 12.sp) }
    }
}
