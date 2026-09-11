package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import dev.vibebridge.core.ChatMsg
import dev.vibebridge.core.NoteKind
import dev.vibebridge.core.NoteMsg
import dev.vibebridge.core.ParseMsg
import dev.vibebridge.core.PromptMsg
import dev.vibebridge.core.PushMsg
import dev.vibebridge.core.PushState
import dev.vibebridge.core.UserIdea
import dev.vibebridge.core.UserPayload
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.BubbleCornerBot
import dev.vibebridge.ui.theme.BubbleCornerUser
import dev.vibebridge.ui.theme.BubbleUser
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Inset
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbMono
import dev.vibebridge.ui.theme.Warning

@Composable
fun BotAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(SurfaceHigh, CircleShape)
            .border(1.dp, dev.vibebridge.ui.theme.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        VbIconView(icon = VbIcon.BOT, color = Accent, size = 15.dp)
    }
}

@Composable
fun UserAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(SurfaceHigh, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        VbIconView(icon = VbIcon.USER, color = TextDim, size = 15.dp)
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BotAvatar()
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until 3) {
                val frac by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(450, delayMillis = i * 130), RepeatMode.Reverse),
                    label = "dot$i"
                )
                Box(
                    Modifier
                        .size(6.dp)
                        .offset(y = (-5 * frac).dp)
                        .background(TextDim, CircleShape)
                )
            }
        }
    }
}

@Composable
fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .background(BubbleUser, BubbleCornerUser)
                .padding(12.dp)
        ) {
            Text(text, color = Text, fontSize = 14.sp)
        }
        Spacer(Modifier.width(8.dp))
        UserAvatar()
    }
}

@Composable
fun PromptBubble(msg: PromptMsg, expanded: Boolean, onToggle: () -> Unit, onCopy: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(Surface, BubbleCornerBot)
                .border(1.dp, dev.vibebridge.ui.theme.Border, BubbleCornerBot)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PROMPT • ${msg.target}", style = VbMono.Label, color = TextDim)
                Spacer(Modifier.weight(1f))
                VbIconView(
                    icon = VbIcon.COPY,
                    color = Accent,
                    size = 16.dp,
                    modifier = Modifier.clickable(onClick = onCopy)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                msg.prompt,
                style = VbMono.CodeSmall,
                color = Text,
                maxLines = if (expanded) Int.MAX_VALUE else 5
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (expanded) "collapse" else "expand • copy and paste into your AI chat",
                style = VbMono.Label,
                color = TextFaint,
                modifier = Modifier.clickable(onClick = onToggle)
            )
        }
    }
}

@Composable
fun PayloadBubble(msg: UserPayload) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .background(BubbleUser, BubbleCornerUser)
                .padding(12.dp)
        ) {
            Text("AI RESPONSE PASTED", style = VbMono.Label, color = TextDim)
            Spacer(Modifier.height(4.dp))
            Text(
                msg.text.take(240) + if (msg.text.length > 240) "…" else "",
                style = VbMono.CodeSmall,
                color = Text
            )
        }
        Spacer(Modifier.width(8.dp))
        UserAvatar()
    }
}

@Composable
fun ParseBubble(
    msg: ParseMsg,
    expanded: Boolean,
    onToggle: () -> Unit,
    onApply: () -> Unit,
    onPush: () -> Unit
) {
    val creates = msg.rows.count { it.kind == "CREATE" }
    val edits = msg.rows.count { it.kind == "EDIT" }
    val deletes = msg.rows.count { it.kind == "DELETE" }
    Row(verticalAlignment = Alignment.Bottom) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(Surface, BubbleCornerBot)
                .border(1.dp, dev.vibebridge.ui.theme.Border, BubbleCornerBot)
                .padding(12.dp)
        ) {
            Text("CHANGES DETECTED • ${msg.total} ops", style = VbMono.Label, color = TextDim)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (creates > 0) VbChipTag("CREATE $creates", Accent)
                if (edits > 0) VbChipTag("EDIT $edits", TextDim)
                if (deletes > 0) VbChipTag("DELETE $deletes", Danger)
            }
            Spacer(Modifier.height(8.dp))
            val visible = if (expanded) msg.rows else msg.rows.take(3)
            visible.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tint = when (r.kind) {
                        "CREATE" -> Accent
                        "DELETE" -> Danger
                        else -> TextDim
                    }
                    VbIconView(
                        icon = if (r.kind == "DELETE") VbIcon.TRASH else VbIcon.DOC,
                        color = tint,
                        size = 14.dp
                    )
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.path, style = VbMono.CodeSmall, color = Text)
                        Text("${r.kind} • ${r.detail}", style = VbMono.Label, color = TextFaint)
                    }
                }
            }
            if (msg.rows.size > 3) {
                Text(
                    if (expanded) "show less" else "show all ${msg.rows.size} files",
                    style = VbMono.Label,
                    color = TextFaint,
                    modifier = Modifier.clickable(onClick = onToggle)
                )
            }
            msg.warnings.forEach { w ->
                Spacer(Modifier.height(6.dp))
                VbBanner(kind = BannerKind.WARN, text = w)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VbButtonSecondary(text = "APPLY LOCALLY", onClick = onApply, modifier = Modifier.weight(1f))
                VbButtonGreen(text = "PUSH TO GITHUB", onClick = onPush, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PushBubble(msg: PushMsg, onOpenRun: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(Surface, BubbleCornerBot)
                .border(1.dp, dev.vibebridge.ui.theme.Border, BubbleCornerBot)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PUSH", style = VbMono.Label, color = TextDim)
                Spacer(Modifier.weight(1f))
                val tag = when (msg.state) {
                    PushState.DONE -> if (msg.conclusion == "success") VbChipTag("CI PASSED", Accent) else VbChipTag("CI ${msg.conclusion?.uppercase() ?: "DONE"}", Warning)
                    PushState.FAILED -> VbChipTag("FAILED", Danger)
                    else -> VbChipTag(msg.state.name, TextDim)
                }
                tag
            }
            Spacer(Modifier.height(6.dp))
            when (msg.state) {
                PushState.PREPARING, PushState.COMMITTING, PushState.POLLING -> {
                    Text(msg.note ?: "working…", style = VbMono.CodeSmall, color = TextDim)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val tr = rememberInfiniteTransition(label = "pushdots")
                        for (i in 0 until 3) {
                            val f by tr.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(450, delayMillis = i * 130), RepeatMode.Reverse),
                                label = "pd$i"
                            )
                            Box(Modifier.size(5.dp).offset(y = (-4 * f).dp).background(TextDim, CircleShape))
                        }
                    }
                }
                PushState.DONE -> {
                    msg.sha?.let { Text("commit ${it.take(7)}", style = VbMono.Code, color = Text) }
                    Spacer(Modifier.height(8.dp))
                    VbButtonSecondary(text = "OPEN RUN", onClick = onOpenRun, modifier = Modifier.fillMaxWidth())
                }
                PushState.FAILED -> {
                    Text(msg.note ?: "push failed", style = VbMono.CodeSmall, color = Danger)
                }
            }
        }
    }
}

@Composable
fun NoteBubble(msg: NoteMsg) {
    val kind = when (msg.kind) {
        NoteKind.INFO -> BannerKind.INFO
        NoteKind.WARN -> BannerKind.WARN
        NoteKind.ERROR -> BannerKind.ERROR
    }
    VbBanner(kind = kind, text = msg.text, modifier = Modifier.padding(start = 36.dp))
}

@Composable
fun VbComposer(
    value: String,
    onValue: (String) -> Unit,
    onSend: () -> Unit,
    onClip: () -> Unit,
    sendEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VbIconButton(icon = VbIcon.GRAB, description = "Insert clipboard", onClick = onClip)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Inset, BubbleCornerBot)
                    .border(1.dp, dev.vibebridge.ui.theme.Border, BubbleCornerBot)
            ) {
                TextField(
                    value = value,
                    onValueChange = onValue,
                    placeholder = { Text("describe your idea, or paste AI output…", color = TextFaint, fontSize = 13.sp) },
                    maxLines = 4,
                    textStyle = dev.vibebridge.ui.theme.VbType.bodyMedium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Text,
                        unfocusedTextColor = Text,
                        cursorColor = Accent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )
            }
            VbIconButton(
                icon = VbIcon.SEND,
                description = "Send",
                onClick = onSend,
                enabled = sendEnabled,
                tint = if (sendEnabled) Accent else TextFaint,
                accent = sendEnabled
            )
        }
        if (value.isNotEmpty()) {
            Text("${value.length} chars", style = VbMono.Label, color = TextFaint, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}
