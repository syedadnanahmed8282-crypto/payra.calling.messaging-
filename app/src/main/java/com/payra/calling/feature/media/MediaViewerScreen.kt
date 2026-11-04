package com.payra.calling.feature.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.feature.chats.MediaTile
import java.util.Locale

private const val FakeVideoSeconds = 24

/**
 * Figma screens 27-30: tap to hide / show the controls, pinch or double-tap to zoom, drag when zoomed.
 * Videos also get a play / pause button and a progress bar (fake playback until a real player is connected).
 */
@Composable
fun MediaViewerScreen(tone: Int, isVideo: Boolean, onBack: () -> Unit) {
    var controlsVisible by remember { mutableStateOf(true) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var playing by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(playing) {
        if (playing) {
            val remaining = (1f - progress.value).coerceAtLeast(0.01f)
            progress.animateTo(1f, tween((FakeVideoSeconds * 1000 * remaining).toInt().coerceAtLeast(200), easing = LinearEasing))
            playing = false
            progress.snapTo(0f)
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offsetX += panChange.x
            offsetY += panChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2A33))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                        }
                    },
                )
            },
    ) {
        MediaTile(
            item = MediaItem(tone, false, ""),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .transformable(transformState),
        )

        // Big play / pause button in the middle for videos
        if (isVideo) {
            AnimatedVisibility(
                visible = controlsVisible || !playing,
                modifier = Modifier.align(Alignment.Center),
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f), initialScale = 0.5f) + fadeIn(),
                exit = scaleOut(targetScale = 0.5f) + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .bouncyClick(onClick = { playing = !playing }, pressedScale = 0.88f)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.40f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = playing,
                        transitionSpec = { (fadeIn(tween(120)) + scaleIn(initialScale = 0.5f)) togetherWith (fadeOut(tween(80)) + scaleOut(targetScale = 0.5f)) },
                        label = "viewerPlay",
                    ) { isPlaying ->
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(160)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
                Spacer(Modifier.weight(1f))
                GlassCircleButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = {})
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(160)),
        ) {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp)) {
                if (isVideo) {
                    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = PayraShapes.Pill, fillAlpha = 0.5f) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val elapsed = (progress.value * FakeVideoSeconds).toInt()
                            Text(
                                text = String.format(Locale.US, "0:%02d", elapsed),
                                color = PayraColors.Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.width(12.dp))
                            Canvas(modifier = Modifier.weight(1f).height(6.dp)) {
                                drawRoundRect(
                                    color = Color(0xFF16323B).copy(alpha = 0.18f),
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                                )
                                drawRoundRect(
                                    color = Color(0xFF1B6F8A),
                                    size = Size(size.width * progress.value, size.height),
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                                )
                                drawCircle(
                                    color = Color(0xFF1B6F8A),
                                    radius = size.height * 1.3f,
                                    center = Offset(size.width * progress.value, size.height / 2f),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("0:$FakeVideoSeconds", color = PayraColors.Muted, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    GlassCircleButton(icon = Icons.Outlined.FileDownload, contentDescription = "Download", onClick = {})
                    GlassCircleButton(
                        icon = Icons.Outlined.ZoomOutMap,
                        contentDescription = "Reset zoom",
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = scale > 1.05f && controlsVisible,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 72.dp),
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f), initialScale = 0.6f) + fadeIn(),
            exit = fadeOut(),
        ) {
            GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.7f) {
                Text(
                    text = String.format(Locale.US, "%.1fx", scale),
                    color = Color(0xFF16323B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }
}
