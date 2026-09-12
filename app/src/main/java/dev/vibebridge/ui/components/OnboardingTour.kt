package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Elevated
import dev.vibebridge.ui.theme.GhostPill
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.WindowBorder

private data class TourStep(val title: String, val body: String)

private val STEPS = listOf(
    TourStep(
        "1 • TYPE AN IDEA",
        "tell vibebridge what you want in plain words. it compiles your idea into a bridge prompt tuned for your target AI."
    ),
    TourStep(
        "2 • PASTE THE REPLY",
        "copy that prompt into qwen, chatgpt or gemini, then paste the AI's answer back here. FILE / EDIT / DELETE blocks parse themselves."
    ),
    TourStep(
        "3 • REVIEW & PUSH",
        "tap any file row to preview its diff. green PUSH uploads blobs, commits and polls CI — watch the build log live under the dots."
    ),
    TourStep(
        "4 • INSTALL OR REWIND",
        "CI green? INSTALL straight from the bubble. CI red? FIX IT, or TIME MACHINE from LIBRARY. the bunny believes in you."
    )
)

@Composable
fun OnboardingTour(onDone: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val s = STEPS[step]
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Elevated, RoundedCornerShape(20.dp))
                .border(1.dp, WindowBorder, RoundedCornerShape(20.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PixelBunny(modifier = Modifier.height(84.dp))
            Text(
                s.title,
                color = Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                s.body,
                color = TextDim,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                STEPS.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (i == step) Accent else GhostPill)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostPillButton("SKIP", onDone)
                GreenPillButton(
                    if (step == STEPS.size - 1) "LET'S GO" else "NEXT",
                    onClick = {
                        if (step == STEPS.size - 1) onDone() else step++
                    }
                )
            }
        }
    }
}
