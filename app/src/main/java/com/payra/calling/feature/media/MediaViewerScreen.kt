package com.payra.calling.feature.media

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.payra.calling.data.DeviceData
import com.payra.calling.data.MediaViewerHolder
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.feature.chats.MediaTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val FakeVideoSeconds = 24

private fun clock(ms: Int): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", total / 60, total % 60)
}

/**
 * Full-screen viewer for the picture or video set in MediaViewerHolder.
 * Photos: pinch / double-tap to zoom, drag when zoomed, tap hides the controls.
 * Videos: real playback with play / pause and a seek bar. Download saves a copy into the app's Download folder.
 */
@Composable
fun MediaViewerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val item = remember { MediaViewerHolder.item }
    val isVideo = item.isVideo
    val real = item.uri.isNotEmpty()

    var controlsVisible by remember { mutableStateOf(true) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // video state
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableIntStateOf(if (real) 0 else FakeVideoSeconds * 1000) }
    var positionMs by remember { mutableIntStateOf(0) }
    val fakeProgress = remember { Animatable(0f) }

    // download state: 0 idle, 1 downloading, 2 done
    var downloadState by remember { mutableIntStateOf(0) }
    val downloadProgress = remember { Animatable(0f) }

    LaunchedEffect(playing, videoView) {
        if (!isVideo) return@LaunchedEffect
        val view = videoView
        if (real && view != null) {
            if (playing) {
                view.start()
                while (playing) {
                    positionMs = view.currentPosition
                    durationMs = view.duration.coerceAtLeast(1)
                    progress = positionMs.toFloat() / durationMs
                    if (!view.isPlaying && positionMs >= durationMs - 150) {
                        playing = false
                        view.seekTo(0)
                        progress = 0f
                        positionMs = 0
                    }
                    delay(100)
                }
            } else if (view.isPlaying) {
                view.pause()
            }
        } else if (!real) {
            if (playing) {
                val remaining = (1f - fakeProgress.value).coerceAtLeast(0.01f)
                fakeProgress.animateTo(1f, tween((FakeVideoSeconds * 1000 * remaining).toInt().coerceAtLeast(200), easing = LinearEasing))
                playing = false
                fakeProgress.snapTo(0f)
            }
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

    fun download() {
        if (downloadState != 0) return
        downloadState = 1
        scope.launch {
            downloadProgress.snapTo(0f)
            downloadProgress.animateTo(0.8f, tween(800, easing = LinearEasing))
            var savedPath: String? = null
            if (real) {
                val name = "payra_${System.currentTimeMillis()}." + if (isVideo) "mp4" else "jpg"
                savedPath = withContext(Dispatchers.IO) { DeviceData.saveToDownloads(context, Uri.parse(item.uri), name)?.absolutePath }
            }
            downloadProgress.animateTo(1f, tween(250))
            downloadState = 2
            Toast.makeText(
                context,
                if (savedPath != null) "Saved to Android/data/${context.packageName}/files/Download" else "Saved",
                Toast.LENGTH_LONG,
            ).show()
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
                        if (!isVideo) {
                            if (scale > 1f) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                scale = 2.5f
                            }
                        }
                    },
                )
            },
    ) {
        if (real && isVideo) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(item.uri))
                        setOnPreparedListener { mp ->
                            durationMs = mp.duration.coerceAtLeast(1)
                            seekTo(1)
                        }
                        setOnCompletionListener {
                            playing = false
                            progress = 0f
                            positionMs = 0
                        }
                        videoView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else if (real) {
            PayraImage(
                model = item.uri,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .transformable(transformState),
                contentScale = ContentScale.Fit,
            )
        } else {
            MediaTile(
                item = item.copy(isVideo = false),
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
        }

        // Big play / pause button for videos
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
                if (MediaViewerHolder.title.isNotEmpty()) {
                    GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.6f) {
                        Text(
                            MediaViewerHolder.title,
                            color = PayraColors.Ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
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
                    val shownProgress = if (real) progress else fakeProgress.value
                    val shownMs = if (real) positionMs else (fakeProgress.value * FakeVideoSeconds * 1000).toInt()
                    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = PayraShapes.Pill, fillAlpha = 0.5f) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(clock(shownMs), color = PayraColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(12.dp))
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .pointerInput(real) {
                                        detectTapGestures { tap ->
                                            val fraction = (tap.x / size.width).coerceIn(0f, 1f)
                                            val view = videoView
                                            if (real && view != null) {
                                                view.seekTo((durationMs * fraction).toInt())
                                                progress = fraction
                                                positionMs = (durationMs * fraction).toInt()
                                            }
                                        }
                                    },
                            ) {
                                val barTop = size.height / 2f - 3.dp.toPx()
                                drawRoundRect(
                                    color = Color(0xFF16323B).copy(alpha = 0.18f),
                                    topLeft = Offset(0f, barTop),
                                    size = Size(size.width, 6.dp.toPx()),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                )
                                drawRoundRect(
                                    color = Color(0xFF1B6F8A),
                                    topLeft = Offset(0f, barTop),
                                    size = Size(size.width * shownProgress, 6.dp.toPx()),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                )
                                drawCircle(
                                    color = Color(0xFF1B6F8A),
                                    radius = 8.dp.toPx(),
                                    center = Offset(size.width * shownProgress, size.height / 2f),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(clock(durationMs), color = PayraColors.Muted, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    // Download: arrow -> ring that fills -> check
                    GlassSurface(modifier = Modifier.size(48.dp), shape = CircleShape, onClick = { download() }) {
                        AnimatedContent(
                            targetState = downloadState,
                            transitionSpec = { (fadeIn(tween(160)) + scaleIn(initialScale = 0.5f)) togetherWith (fadeOut(tween(100)) + scaleOut(targetScale = 0.5f)) },
                            label = "viewerDownload",
                            modifier = Modifier.align(Alignment.Center),
                        ) { st ->
                            when (st) {
                                0 -> Icon(Icons.Outlined.FileDownload, contentDescription = "Download", tint = PayraColors.Ink, modifier = Modifier.size(22.dp))
                                1 -> Canvas(modifier = Modifier.size(24.dp)) {
                                    val w = 3.dp.toPx()
                                    drawArc(
                                        color = Color(0xFF16323B).copy(alpha = 0.2f),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = w),
                                    )
                                    drawArc(
                                        color = Color(0xFF1B6F8A),
                                        startAngle = -90f,
                                        sweepAngle = 360f * downloadProgress.value,
                                        useCenter = false,
                                        style = Stroke(width = w, cap = StrokeCap.Round),
                                    )
                                }
                                else -> Icon(Icons.Outlined.CheckCircle, contentDescription = "Saved", tint = PayraColors.Success, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    if (!isVideo) {
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
