package dev.vibebridge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

object VbAnim {
    const val ENTER_MS = 240
    const val STAGGER_MS = 50
    const val MSG_MS = 260
}

fun staggerEnter(index: Int): EnterTransition =
    fadeIn(tween(VbAnim.ENTER_MS, index * VbAnim.STAGGER_MS)) +
        slideInVertically(tween(320, index * VbAnim.STAGGER_MS, easing = FastOutSlowInEasing)) { it / 10 }

@Composable
fun Stagger(index: Int, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    AnimatedVisibility(visible = shown, enter = staggerEnter(index)) { content() }
}

@Composable
fun MessageEnter(content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(30)
        shown = true
    }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(VbAnim.MSG_MS)) + slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 6 },
        exit = fadeOut(tween(120))
    ) { content() }
}

@Composable
fun ExpandCollapse(expanded: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(tween(220, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
        exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
    ) { content() }
}

@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )
    return scale
}

fun Modifier.pressScale(scale: Float): Modifier =
    this.graphicsLayer { scaleX = scale; scaleY = scale }

@Composable
fun rememberPulse(active: Boolean): Float {
    return animateFloatAsState(
        targetValue = if (active) 1.02f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "pulse"
    ).value
}
