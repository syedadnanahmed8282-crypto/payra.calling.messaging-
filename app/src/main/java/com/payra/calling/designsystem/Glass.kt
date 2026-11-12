package com.payra.calling.designsystem

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.payra.calling.data.UiPrefs

/**
 * The one place that draws "glass". Every screen uses this, so the look can be changed here only.
 *
 * - Inside a [GlassScaffold] bar: content that scrolls underneath is blurred (Android 12+),
 *   so colors behind the bar shine through. On older Android a tinted frosted fill is used instead.
 * - Everywhere: a diagonal sheen (light top-left, faint cyan bottom-right) and a bright edge,
 *   like light catching real glass.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = PayraShapes.Card,
    fillAlpha: Float = 0.58f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = LocalGlassBackdrop.current
    val onBar = backdrop != null
    val blurred = onBar && Build.VERSION.SDK_INT >= 31 && !UiPrefs.reduceEffects
    val frosted = onBar && !blurred
    val alpha = when {
        blurred -> minOf(fillAlpha, 0.40f)
        frosted -> 0.80f
        else -> fillAlpha
    }

    val clickModifier = if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier
    val blurModifier = if (blurred && backdrop != null) Modifier.glassBlurBehind(backdrop) else Modifier

    Box(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .then(blurModifier)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (alpha + 0.16f).coerceAtMost(1f)),
                        Color.White.copy(alpha = alpha),
                        PayraColors.Glow.copy(alpha = (alpha * 0.85f).coerceAtMost(1f)),
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .then(
                if (frosted) {
                    // Older phones cannot blur: paint soft colored reflections inside the frosted glass instead.
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(PayraColors.Primary.copy(alpha = 0.16f), Color.Transparent),
                                center = Offset(size.width * 0.12f, size.height * 0.5f),
                                radius = size.width * 0.55f,
                            ),
                            radius = size.width * 0.55f,
                            center = Offset(size.width * 0.12f, size.height * 0.5f),
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(PayraColors.AccentLight.copy(alpha = 0.32f), Color.Transparent),
                                center = Offset(size.width * 0.9f, size.height * 0.4f),
                                radius = size.width * 0.5f,
                            ),
                            radius = size.width * 0.5f,
                            center = Offset(size.width * 0.9f, size.height * 0.4f),
                        )
                    }
                } else {
                    Modifier
                },
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.75f)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
                shape,
            ),
        content = content,
    )
}
