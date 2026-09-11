package dev.vibebridge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
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

object VbAnim {
    const val ENTER_MS = 260
    const val STAGGER_MS = 55
}

fun staggerEnter(index: Int): EnterTransition =
    fadeIn(tween(VbAnim.ENTER_MS, index * VbAnim.STAGGER_MS)) +
        slideInVertically(tween(340, index * VbAnim.STAGGER_MS, easing = FastOutSlowInEasing)) { it / 8 }

@Composable
fun Stagger(index: Int, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    AnimatedVisibility(visible = shown, enter = staggerEnter(index)) { content() }
}

@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "pressScale"
    )
    return scale
}

fun Modifier.pressScale(scale: Float): Modifier =
    this.graphicsLayer { scaleX = scale; scaleY = scale }
