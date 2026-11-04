package com.payra.calling.designsystem

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Real "frosted glass" (Android 12+): the screen content is recorded into a layer, and every glass bar
 * draws a blurred copy of the part of that layer that lies behind it. Older Android versions skip the blur
 * (GlassSurface then uses a stronger frosted fill instead).
 */
class GlassBackdrop(val layer: GraphicsLayer) {
    var origin by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

val LocalGlassBackdrop = compositionLocalOf<GlassBackdrop?> { null }

/** Put this on the content that should be visible *through* glass bars. */
fun Modifier.glassSource(backdrop: GlassBackdrop): Modifier = this
    .onGloballyPositioned { backdrop.origin = it.positionInRoot() }
    .drawWithContent {
        backdrop.layer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(backdrop.layer)
    }

/** Draws the blurred backdrop behind this node. Only call on Android 12+ (SDK 31). */
@Composable
fun Modifier.glassBlurBehind(backdrop: GlassBackdrop, radius: Dp = 22.dp): Modifier {
    val blurLayer = rememberGraphicsLayer()
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    var origin by remember { mutableStateOf(Offset.Zero) }

    SideEffect {
        if (Build.VERSION.SDK_INT >= 31) {
            blurLayer.renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
        }
    }

    return this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawWithContent {
            val dx = origin.x - backdrop.origin.x
            val dy = origin.y - backdrop.origin.y
            blurLayer.record {
                translate(left = -dx, top = -dy) {
                    drawLayer(backdrop.layer)
                }
            }
            drawLayer(blurLayer)
            drawContent()
        }
}
