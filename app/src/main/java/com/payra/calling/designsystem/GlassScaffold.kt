package com.payra.calling.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Screen layout where the content runs *under* the glass bars (top bar / bottom bar) and shows through them.
 * The screen is edge to edge. The content lambda receives the space the bars cover.
 */
@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    imeAware: Boolean = false,
    wallpaper: WallpaperPalette = Wallpapers.all[0],
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberGlassBackdrop()
    val density = LocalDensity.current
    var topPx by remember { mutableIntStateOf(0) }
    var bottomPx by remember { mutableIntStateOf(0) }
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPad = if (topBar != null) with(density) { topPx.toDp() } else statusTop
    val bottomPad = if (bottomBar != null) with(density) { bottomPx.toDp() } else navBottom

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (imeAware) Modifier.imePadding() else Modifier),
    ) {
        GlowBackground(modifier = Modifier.glassSource(backdrop), palette = wallpaper) {
            content(PaddingValues(top = topPad, bottom = bottomPad))
        }

        // Very light dark fade so status-bar icons stay readable over any content (no white haze).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusTop + 10.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A2A33).copy(alpha = 0.10f), Color.Transparent),
                    ),
                ),
        )

        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
            if (topBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { topPx = it.height }
                        .statusBarsPadding(),
                ) { topBar() }
            }
            if (bottomBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { bottomPx = it.height }
                        .navigationBarsPadding(),
                ) { bottomBar() }
            }
        }
    }
}
