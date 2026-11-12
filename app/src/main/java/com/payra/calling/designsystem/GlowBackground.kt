package com.payra.calling.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.payra.calling.data.UiPrefs

/**
 * Soft glows behind the glass (the "liquid glass" backdrop). The glows drift very slowly.
 * [palette] lets a chat use its own wallpaper colors.
 */
@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
    palette: WallpaperPalette = Wallpapers.all[0],
    base: Color = palette.base,
    animated: Boolean = !UiPrefs.reduceEffects,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "glowDrift")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phase",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                val w = size.width
                val h = size.height
                val p = if (animated) phase.value else 0.5f
                val dx = (p - 0.5f) * w * 0.12f
                val dy = (p - 0.5f) * h * 0.05f
                fun glow(cx: Float, cy: Float, r: Float, color: Color, alpha: Float) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r,
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                    )
                }
                glow(w * 0.05f + dx, h * 0.04f + dy, w * 0.75f, palette.glowA, 0.38f)
                glow(w * 0.90f - dx, h * 0.22f + dy, w * 0.65f, palette.glowB, 0.45f)
                glow(w * 0.05f + dx, h * 0.75f - dy, w * 0.70f, palette.glowC, 0.55f)
                glow(w * 0.95f - dx, h * 0.92f - dy, w * 0.60f, palette.glowA, 0.25f)
            },
        content = content,
    )
}
