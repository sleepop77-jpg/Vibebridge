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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.ChatMsg
import dev.vibebridge.core.NoteMsg
import dev.vibebridge.core.ParseMsg
import dev.vibebridge.core.PromptMsg
import dev.vibebridge.core.PushMsg
import dev.vibebridge.core.PromptTemplates
import dev.vibebridge.core.UserIdea
import dev.vibebridge.core.UserPayload
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.ui.components.MessageEnter
import dev.vibebridge.ui.components.ParseBubble
import dev.vibebridge.ui.components.PayloadBubble
import dev.vibebridge.ui.components.PromptBubble
import dev.vibebridge.ui.components.PushBubble
import dev.vibebridge.ui.components.NoteBubble
import dev.vibebridge.ui.components.TypingIndicator
import dev.vibebridge.ui.components.UserBubble
import dev.vibebridge.ui.components.VbComposer
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbIconView
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.VbMono
import dev.vibebridge.viewmodel.ChatViewModel

@Composable
fun ChatScreen(vm: ChatViewModel, openSettings: () -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { vm.startClipWatch() }
    LaunchedEffect(ui.messages.size, ui.thinking) {
        if (ui.messages.isNotEmpty() || ui.thinking) {
            listState.animateScrollToItem(ui.messages.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VbWordmark(height = 20.dp, modifier = Modifier.weight(1f))
            VbIconButton(icon = VbIcon.PLUS, description = "New chat", onClick = vm::newChat)
            VbIconButton(icon = VbIcon.GEAR, description = "Settings", onClick = openSettings)
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (ui.messages.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("START A CHANGE", style = VbMono.Label, color = TextDim)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PromptTemplates.BUILTINS.forEach { b ->
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Border, RoundedCornerShape(20.dp))
                                            .background(SurfaceHigh, RoundedCornerShape(20.dp))
                                            .clickable { vm.sendIdea(b.idea) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(b.name, color = TextDim, fontSize = 12.sp)
                                    }
                                }
                            }
                            Text(
                                "type an idea below — vibebridge compiles it into a bridge prompt, you paste the AI reply back here, and the app parses, previews and pushes it.",
                                color = TextDim,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                items(ui.messages, key = { it.id }) { msg ->
                    MessageEnter {
                        Bubble(
                            msg = msg,
                            expanded = expanded[msg.id] == true,
                            onToggle = { expanded[msg.id] = !(expanded[msg.id] == true) },
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
                            }
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
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, Accent, RoundedCornerShape(10.dp))
                    .background(SurfaceHigh, RoundedCornerShape(10.dp))
                    .clickable { vm.clipIntoInput() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VbIconView(icon = VbIcon.GRAB, color = Accent, size = 15.dp)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    "bridge payload detected on clipboard — tap to insert",
                    color = Text,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                VbIconButton(icon = VbIcon.CROSS, description = "Dismiss", onClick = vm::dismissClip)
            }
        }

        VbComposer(
            value = ui.input,
            onValue = vm::setInput,
            onSend = vm::send,
            onClip = vm::clipIntoInput,
            sendEnabled = ui.input.isNotBlank() && !ui.thinking,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Bubble(
    msg: ChatMsg,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCopyPrompt: (String) -> Unit,
    onApply: () -> Unit,
    onPush: () -> Unit,
    onOpenRun: () -> Unit
) {
    when (msg) {
        is UserIdea -> UserBubble(msg.text)
        is PromptMsg -> PromptBubble(msg, expanded, onToggle, { onCopyPrompt(msg.prompt) })
        is UserPayload -> PayloadBubble(msg)
        is ParseMsg -> ParseBubble(msg, expanded, onToggle, onApply, onPush)
        is PushMsg -> PushBubble(msg, onOpenRun)
        is NoteMsg -> NoteBubble(msg)
    }
}
