package com.payra.calling.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Payra motion toolkit. Every screen uses these, so the whole app moves the same way:
 * springs instead of linear tweens, staggered entrances, press feedback on everything tappable.
 */
object Motion {
    const val ScreenMillis = 380
    val Bouncy = spring<Float>(dampingRatio = 0.55f, stiffness = 420f)
    val Soft = spring<Float>(dampingRatio = 0.8f, stiffness = 280f)
}

/** Squishy press feedback + click, with no ripple. Use for every tappable element. */
@Composable
fun Modifier.bouncyClick(onClick: () -> Unit, pressedScale: Float = 0.94f): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

/** Fade + slide up when first shown. [index] staggers a column of items one after another. */
@Composable
fun Modifier.enterFromBottom(index: Int = 0, delayPerItem: Long = 55L, distance: Dp = 28.dp): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        progress.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 260f))
    }
    return this.graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * distance.toPx()
    }
}

/** A message-bubble style pop: grows out of its bottom corner with a small overshoot. */
@Composable
fun Modifier.popIn(enabled: Boolean = true, fromEnd: Boolean = false): Modifier {
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (enabled) progress.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 380f))
    }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        val s = 0.78f + 0.22f * p
        scaleX = s
        scaleY = s
        translationY = (1f - p) * 36.dp.toPx()
        transformOrigin = TransformOrigin(if (fromEnd) 1f else 0f, 1f)
    }
}

/** Quick left-right shake. Change [trigger] (for example count up) to play it. */
@Composable
fun Modifier.shake(trigger: Int): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            offset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 460
                    -16f at 70
                    14f at 150
                    -10f at 230
                    7f at 310
                    -3f at 390
                    0f at 460
                },
            )
        }
    }
    return this.graphicsLayer { translationX = offset.value * density }
}

/** Three bouncing dots: the "typing..." indicator and loading dots. */
@Composable
fun BouncingDots(color: Color, modifier: Modifier = Modifier, dotSize: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val lift = transition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(420, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 140),
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer { translationY = lift.value * density }
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
