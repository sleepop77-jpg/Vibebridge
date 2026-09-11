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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import dev.vibebridge.core.NoteKind
import dev.vibebridge.core.NoteMsg
import dev.vibebridge.core.ParseMsg
import dev.vibebridge.core.PromptMsg
import dev.vibebridge.core.PushMsg
import dev.vibebridge.core.PushState
import dev.vibebridge.core.UserPayload
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.ButtonGreen
import dev.vibebridge.ui.theme.BubbleUser
import dev.vibebridge.ui.theme.ComposerBg
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Elevated
import dev.vibebridge.ui.theme.GhostPill
import dev.vibebridge.ui.theme.PureBlack
import dev.vibebridge.ui.theme.PureWhite
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbMono
import dev.vibebridge.ui.theme.VbType
import dev.vibebridge.ui.theme.Warning
import dev.vibebridge.ui.theme.WindowBorder

private val ArrowUp = VbIconKind(
    listOf(
        Seg(12f, 18f, 12f, 7f),
        Seg(6.5f, 12f, 12f, 6.5f),
        Seg(12f, 6.5f, 17.5f, 12f)
    )
)

@Composable
fun GhostPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextDim
) {
    Box(
        modifier = modifier
            .border(1.dp, WindowBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GreenPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ButtonGreen, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun UserBubble(text: String) {
    var expanded by remember { mutableStateOf(false) }
    val isLong = text.length > 200
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .background(BubbleUser, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text,
                    style = VbType.bodyLarge,
                    color = Text,
                    maxLines = if (expanded || !isLong) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLong) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (expanded) "show less" else "show more",
                        style = VbMono.Label,
                        color = TextFaint,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}

@Composable
fun PromptBubble(
    msg: PromptMsg,
    onCopy: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isLong = msg.prompt.length > 250
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("PROMPT • ${msg.target}", style = VbMono.Label, color = TextFaint)
        Spacer(Modifier.height(4.dp))
        Text(
            msg.prompt,
            style = VbType.bodyLarge,
            color = Text,
            maxLines = if (expanded || !isLong) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.clickable(onClick = onCopy).padding(2.dp)) {
                VbIconView(icon = VbIcon.COPY, color = TextFaint, size = 15.dp)
            }
            if (isLong) {
                Box(Modifier.clickable(onClick = { expanded = !expanded }).padding(2.dp)) {
                    Text(
                        text = if (expanded) "collapse" else "expand",
                        style = VbMono.Label,
                        color = TextFaint
                    )
                }
            }
        }
    }
}

@Composable
fun PayloadBubble(msg: UserPayload) {
    var expanded by remember { mutableStateOf(false) }
    val isLong = msg.text.length > 200
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .background(BubbleUser, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("AI RESPONSE PASTED", style = VbMono.Label, color = TextFaint)
                Spacer(Modifier.height(3.dp))
                Text(
                    msg.text,
                    style = VbMono.CodeSmall,
                    color = TextDim,
                    maxLines = if (expanded || !isLong) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLong) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (expanded) "show less" else "show more",
                        style = VbMono.Label,
                        color = TextFaint,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}

@Composable
fun ParseBubble(
    msg: ParseMsg,
    onApply: () -> Unit,
    onPush: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val creates = msg.rows.count { it.kind == "CREATE" }
    val edits = msg.rows.count { it.kind == "EDIT" }
    val deletes = msg.rows.count { it.kind == "DELETE" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Elevated, RoundedCornerShape(16.dp))
            .border(1.dp, WindowBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CHANGES", style = VbMono.Label, color = TextDim)
            if (creates > 0) TagChip("CREATE $creates", Accent)
            if (edits > 0) TagChip("EDIT $edits", TextFaint)
            if (deletes > 0) TagChip("DELETE $deletes", Danger)
        }
        Spacer(Modifier.height(8.dp))
        val visible = if (expanded || msg.rows.size <= 3) msg.rows else msg.rows.take(3)
        visible.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tint = when (r.kind) {
                    "CREATE" -> Accent
                    "DELETE" -> Danger
                    else -> TextDim
                }
                VbIconView(icon = if (r.kind == "DELETE") VbIcon.TRASH else VbIcon.DOC, color = tint, size = 14.dp)
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
                modifier = Modifier.clickable(onClick = { expanded = !expanded })
            )
        }
        msg.warnings.forEach { w ->
            Spacer(Modifier.height(6.dp))
            Text("! $w", style = VbMono.CodeSmall, color = Warning)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GhostPillButton("APPLY LOCALLY", onApply)
            GreenPillButton("PUSH TO GITHUB", onPush)
        }
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text, color = if (color == TextFaint) Text else PureBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PushBubble(
    msg: PushMsg,
    onOpenRun: () -> Unit,
    onCopyErrors: () -> Unit,
    onSaveMd: () -> Unit,
    onSaveApk: () -> Unit,
    onShareApk: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Elevated, RoundedCornerShape(16.dp))
            .border(1.dp, WindowBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PUSH", style = VbMono.Label, color = TextDim)
            when (msg.state) {
                PushState.DONE -> TagChip(
                    if (msg.conclusion == "success") "CI PASSED" else "CI ${msg.conclusion?.uppercase() ?: "DONE"}",
                    if (msg.conclusion == "success") Accent else Warning
                )
                PushState.FAILED -> TagChip("FAILED", Danger)
                else -> TagChip(msg.state.name, TextFaint)
            }
        }
        Spacer(Modifier.height(6.dp))
        when (msg.state) {
            PushState.PREPARING, PushState.COMMITTING, PushState.POLLING -> {
                Text(msg.note ?: "working…", style = VbMono.CodeSmall, color = TextDim)
                Spacer(Modifier.height(6.dp))
                BounceDots()
            }
            PushState.DONE -> {
                msg.sha?.let { Text("commit ${it.take(7)}", style = VbMono.Code, color = Text) }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostPillButton("OPEN RUN", onOpenRun)
                    if (msg.conclusion == "success") {
                        GhostPillButton("SAVE APK", onSaveApk)
                    } else {
                        GhostPillButton("COPY ERRORS", onCopyErrors)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (msg.conclusion == "success") {
                        GhostPillButton("SHARE APK", onShareApk)
                    } else {
                        GhostPillButton("SAVE .MD", onSaveMd)
                    }
                }
            }
            PushState.FAILED -> {
                Text(msg.note ?: "push failed", style = VbMono.CodeSmall, color = Danger)
            }
        }
    }
}

@Composable
fun NoteBubble(msg: NoteMsg) {
    val color = when (msg.kind) {
        NoteKind.INFO -> TextFaint
        NoteKind.WARN -> Warning
        NoteKind.ERROR -> Danger
    }
    Text(msg.text, style = VbMono.CodeSmall, color = color, modifier = Modifier.padding(start = 8.dp))
}

@Composable
fun BounceDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until 3) {
            val f by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(450, delayMillis = i * 130), RepeatMode.Reverse),
                label = "dot$i"
            )
            Box(Modifier.size(5.dp).offset(y = (-4 * f).dp).background(TextDim, CircleShape))
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BounceDots()
    }
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ComposerBg, RoundedCornerShape(28.dp))
            .border(1.dp, WindowBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(GhostPill, CircleShape)
                .clickable(onClick = onClip),
            contentAlignment = Alignment.Center
        ) {
            VbIconView(icon = VbIcon.GRAB, color = TextDim, size = 15.dp)
        }
        Spacer(Modifier.width(6.dp))
        TextField(
            value = value,
            onValueChange = onValue,
            placeholder = { Text("Describe your change or paste AI output", color = TextFaint, fontSize = 13.sp) },
            maxLines = 3,
            textStyle = VbType.bodyMedium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Text,
                unfocusedTextColor = Text,
                cursorColor = Text,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (sendEnabled) PureWhite else GhostPill, CircleShape)
                .clickable(enabled = sendEnabled, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            VbIconView(icon = ArrowUp, color = if (sendEnabled) PureBlack else TextFaint, size = 16.dp)
        }
    }
}
