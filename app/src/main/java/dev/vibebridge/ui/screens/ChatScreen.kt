package dev.vibebridge.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.ChatMsg
import dev.vibebridge.core.NoteMsg
import dev.vibebridge.core.ParseMsg
import dev.vibebridge.core.PromptMsg
import dev.vibebridge.core.PromptTemplates
import dev.vibebridge.core.PushMsg
import dev.vibebridge.core.UserIdea
import dev.vibebridge.core.UserPayload
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.ui.components.GhostPillButton
import dev.vibebridge.ui.components.MessageEnter
import dev.vibebridge.ui.components.NoteBubble
import dev.vibebridge.ui.components.ParseBubble
import dev.vibebridge.ui.components.PayloadBubble
import dev.vibebridge.ui.components.PixelBunny
import dev.vibebridge.ui.components.PromptBubble
import dev.vibebridge.ui.components.PushBubble
import dev.vibebridge.ui.components.StarField
import dev.vibebridge.ui.components.TipLine
import dev.vibebridge.ui.components.TypingIndicator
import dev.vibebridge.ui.components.UserBubble
import dev.vibebridge.ui.components.VbComposer
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconView
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.Elevated
import dev.vibebridge.ui.theme.GhostPill
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbType
import dev.vibebridge.ui.theme.WindowBorder
import dev.vibebridge.viewmodel.ChatViewModel

private val MODELS = listOf(
    "QWEN STUDIO" to "free • bridge-tuned",
    "CHATGPT" to "no markdown fences",
    "GEMINI" to "small hunks"
)

@Composable
fun ChatScreen(vm: ChatViewModel, openSettings: () -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val listState = rememberLazyListState()
    var target by remember { mutableStateOf(vm.currentTarget()) }
    var strict by remember { mutableStateOf(vm.currentStrict()) }
    val templates = remember { vm.templateOptions() }
    val files = remember(ui.messages.size) { vm.sandboxFiles() }

    LaunchedEffect(Unit) { vm.startClipWatch() }
    LaunchedEffect(ui.messages.size, ui.thinking) {
        if (ui.messages.isNotEmpty() || ui.thinking) {
            listState.animateScrollToItem(ui.messages.size)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarField()
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VbWordmark(height = 18.dp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .background(GhostPill, CircleShape)
                        .clickable(onClick = openSettings)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VbIconView(icon = VbIcon.GEAR, color = TextDim, size = 16.dp)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (ui.messages.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Spacer(Modifier.height(8.dp))
                                PixelBunny(modifier = Modifier.height(104.dp))
                                VbWordmark(height = 24.dp, showTagline = true)
                                TipLine(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PromptTemplates.BUILTINS.forEach { b ->
                                        GhostPillButton(b.name, { vm.sendIdea(b.idea) })
                                    }
                                }
                            }
                        }
                    }
                    items(ui.messages, key = { it.id }) { msg ->
                        MessageEnter {
                            Bubble(
                                msg = msg,
                                onCopyPrompt = { p ->
                                    VbClipboard.copy(ctx, "vibe-prompt", p)
                                    Toast.makeText(ctx, "prompt copied", Toast.LENGTH_SHORT).show()
                                },
                                onApply = vm::applyLocal,
                                onPush = vm::push,
                                onOpenRun = {
                                    if (msg is PushMsg) {
                                        msg.runUrl?.let { url ->
                                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                        }
                                    }
                                },
                                onCopyErrors = { if (msg is PushMsg) msg.runId?.let { vm.copyErrors(it) } },
                                onSaveMd = { if (msg is PushMsg) msg.runId?.let { vm.saveErrorsMd(it, msg.sha) } },
                                onSaveApk = { if (msg is PushMsg) msg.runId?.let { vm.saveApkToDownloads(it, msg.sha) } },
                                onShareApk = { if (msg is PushMsg) msg.runId?.let { vm.shareApk(it) } },
                                onFixIt = { if (msg is PushMsg) vm.fixIt(msg.runId, msg.note) }
                            )
                        }
                    }
                    if (ui.thinking) {
                        item { TypingIndicator() }
                    }
                }
            }

            ui.clipHint?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .background(Elevated, RoundedCornerShape(12.dp))
                        .border(1.dp, WindowBorder, RoundedCornerShape(12.dp))
                        .clickable { vm.clipIntoInput() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VbIconView(icon = VbIcon.GRAB, color = Text, size = 14.dp)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        "bridge payload on clipboard — tap to insert",
                        color = Text,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    VbIconView(icon = VbIcon.CROSS, color = TextFaint, size = 13.dp, modifier = Modifier.clickable { vm.dismissClip() })
                }
            }

            VbComposer(
                value = ui.input,
                onValue = vm::setInput,
                onSend = vm::send,
                onClip = vm::clipIntoInput,
                sendEnabled = ui.input.isNotBlank() && !ui.thinking,
                models = MODELS,
                currentModel = target,
                onModel = { t -> target = t; vm.setTarget(t) },
                templates = templates,
                onTemplate = { name -> templates.firstOrNull { it.first == name }?.second?.let { vm.setInput(it) } },
                files = files,
                onFile = { vm.attachFile(it) },
                strict = strict,
                onStrict = { strict = vm.toggleStrict() },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun Bubble(
    msg: ChatMsg,
    onCopyPrompt: (String) -> Unit,
    onApply: () -> Unit,
    onPush: () -> Unit,
    onOpenRun: () -> Unit,
    onCopyErrors: () -> Unit,
    onSaveMd: () -> Unit,
    onSaveApk: () -> Unit,
    onShareApk: () -> Unit,
    onFixIt: () -> Unit
) {
    when (msg) {
        is UserIdea -> UserBubble(msg.text)
        is PromptMsg -> PromptBubble(msg, { onCopyPrompt(msg.prompt) })
        is UserPayload -> PayloadBubble(msg)
        is ParseMsg -> ParseBubble(msg, onApply, onPush)
        is PushMsg -> PushBubble(msg, onOpenRun, onCopyErrors, onSaveMd, onSaveApk, onShareApk, onFixIt)
        is NoteMsg -> NoteBubble(msg)
    }
}
