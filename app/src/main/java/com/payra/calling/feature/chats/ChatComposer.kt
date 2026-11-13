package com.payra.calling.feature.chats

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class VoiceMode { Idle, Holding, Locked }

private fun formatClock(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

/** Removes the last typed character, keeping emoji (which use two characters) whole. */
fun String.dropLastEmojiUnit(): String {
    if (isEmpty()) return this
    var end = length
    if (this[end - 1] == '\uFE0F' && end >= 2) end -= 1
    val cp = codePointBefore(end)
    return substring(0, end - Character.charCount(cp))
}

/**
 * The message bar. Tap the mic: records until you tap send (locked).
 * Hold the mic: records while you hold; slide left to cancel, slide up to lock, release to send.
 */
@Composable
fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    isEditing: Boolean,
    emojiOpen: Boolean,
    onEmojiOpenChange: (Boolean) -> Unit,
    onSend: (String) -> Unit,
    onSendVoice: (String, String) -> Unit,
    onAttach: () -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val recorder = remember { VoiceRecorder(context) }

    var mode by remember { mutableStateOf(VoiceMode.Idle) }
    var seconds by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val cancelPx = with(density) { 110.dp.toPx() }
    val lockPx = with(density) { 80.dp.toPx() }

    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) Toast.makeText(context, "Microphone allowed. Hold or tap the mic to record.", Toast.LENGTH_SHORT).show()
    }

    fun startRecording(): Boolean {
        val granted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        if (!recorder.start()) {
            Toast.makeText(context, "Could not start recording", Toast.LENGTH_SHORT).show()
            return false
        }
        seconds = 0
        paused = false
        dragX = 0f
        dragY = 0f
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        return true
    }

    fun finish(send: Boolean) {
        val result = if (send) recorder.stop() else {
            recorder.cancel()
            null
        }
        mode = VoiceMode.Idle
        paused = false
        seconds = 0
        dragX = 0f
        dragY = 0f
        if (result != null) onSendVoice(formatClock(result.second), result.first)
    }

    LaunchedEffect(mode, paused) {
        while (mode != VoiceMode.Idle && !paused) {
            seconds = recorder.elapsedSeconds()
            delay(250)
        }
    }

    val latestDraft by rememberUpdatedState(draft)
    val latestMode by rememberUpdatedState(mode)

    val hasText = draft.isNotBlank()
    val micScale by animateFloatAsState(
        targetValue = if (mode == VoiceMode.Holding) 1.45f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "micScale",
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "composerMode",
            ) { m ->
                when (m) {
                    VoiceMode.Idle -> TextPill(
                        draft = draft,
                        onDraftChange = onDraftChange,
                        emojiOpen = emojiOpen,
                        onEmojiOpenChange = onEmojiOpenChange,
                        onAttach = onAttach,
                    )
                    VoiceMode.Holding -> HoldingPill(seconds = seconds, cancelProgress = (-dragX / cancelPx).coerceIn(0f, 1f))
                    VoiceMode.Locked -> LockedPill(
                        seconds = seconds,
                        paused = paused,
                        canPause = recorder.canPause,
                        onDelete = { finish(false) },
                        onTogglePause = {
                            if (paused) {
                                if (recorder.resume()) paused = false
                            } else {
                                if (recorder.pause()) {
                                    seconds = recorder.elapsedSeconds()
                                    paused = true
                                }
                            }
                        },
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))

        Box {
            // The little "lock" hint that rises while you slide up
            androidx.compose.animation.AnimatedVisibility(
                visible = mode == VoiceMode.Holding,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-104).dp),
                enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.6f),
                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.6f),
            ) {
                val progress = (-dragY / lockPx).coerceIn(0f, 1f)
                GlassSurface(
                    modifier = Modifier.offset { IntOffset(0, (-progress * 30.dp.toPx()).roundToInt()) },
                    shape = RoundedCornerShape(24.dp),
                    fillAlpha = 0.9f,
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "Slide up to lock",
                            tint = if (progress >= 1f) PayraColors.Primary else PayraColors.Muted,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = micScale
                        scaleY = micScale
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val longPress = awaitLongPressOrCancellation(down.id)
                            if (longPress == null) {
                                // quick tap
                                val text = latestDraft.trim()
                                when {
                                    latestMode == VoiceMode.Locked -> finish(true)
                                    text.isNotEmpty() -> onSend(text)
                                    latestMode == VoiceMode.Idle -> if (startRecording()) mode = VoiceMode.Locked
                                }
                            } else if (latestMode == VoiceMode.Idle && latestDraft.isBlank()) {
                                // hold to record
                                if (startRecording()) {
                                    mode = VoiceMode.Holding
                                    do {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        dragX = change.position.x - down.position.x
                                        dragY = change.position.y - down.position.y
                                        change.consume()
                                    } while (change.pressed)
                                    when {
                                        dragX < -cancelPx -> {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            finish(false)
                                        }
                                        dragY < -lockPx -> {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            mode = VoiceMode.Locked
                                            dragX = 0f
                                            dragY = 0f
                                        }
                                        else -> finish(true)
                                    }
                                }
                            }
                        }
                    }
                    .clip(CircleShape)
                    .background(if (mode == VoiceMode.Holding) PayraColors.Danger else PayraColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                val showSend = hasText || mode == VoiceMode.Locked
                AnimatedContent(
                    targetState = showSend,
                    transitionSpec = {
                        (fadeIn(tween(160)) + scaleIn(animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f), initialScale = 0.4f)) togetherWith
                            (fadeOut(tween(100)) + scaleOut(targetScale = 0.4f))
                    },
                    label = "micIcon",
                ) { send ->
                    Icon(
                        imageVector = if (send) (if (isEditing) Icons.Outlined.Check else Icons.AutoMirrored.Filled.Send) else Icons.Outlined.Mic,
                        contentDescription = if (send) "Send" else "Hold to record a voice message",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TextPill(
    draft: String,
    onDraftChange: (String) -> Unit,
    emojiOpen: Boolean,
    onEmojiOpenChange: (Boolean) -> Unit,
    onAttach: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    GlassSurface(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = PayraShapes.Pill) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = emojiOpen,
                transitionSpec = { (fadeIn(tween(140)) + scaleIn(initialScale = 0.6f)) togetherWith (fadeOut(tween(90)) + scaleOut(targetScale = 0.6f)) },
                label = "emojiToggle",
            ) { open ->
                Icon(
                    imageVector = if (open) Icons.Outlined.Keyboard else Icons.Outlined.EmojiEmotions,
                    contentDescription = if (open) "Keyboard" else "Emoji",
                    tint = if (open) PayraColors.Primary else PayraColors.Muted,
                    modifier = Modifier
                        .size(24.dp)
                        .bouncyClick(
                            onClick = {
                                if (!emojiOpen) {
                                    keyboard?.hide()
                                    focusManager.clearFocus()
                                    onEmojiOpenChange(true)
                                } else {
                                    onEmojiOpenChange(false)
                                    focusRequester.requestFocus()
                                    keyboard?.show()
                                }
                            },
                            pressedScale = 0.8f,
                        ),
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (draft.isEmpty()) {
                    Text("Message", color = PayraColors.Muted, fontSize = 16.sp)
                }
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    maxLines = 4,
                    textStyle = TextStyle(color = PayraColors.Ink, fontSize = 16.sp),
                    cursorBrush = SolidColor(PayraColors.Primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (it.isFocused && emojiOpen) onEmojiOpenChange(false) },
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.Outlined.AttachFile,
                contentDescription = "Attach",
                tint = PayraColors.Muted,
                modifier = Modifier.size(24.dp).bouncyClick(onClick = onAttach, pressedScale = 0.8f),
            )
        }
    }
}

/** While you hold the mic: red dot, timer and "slide to cancel" that turns red as you slide. */
@Composable
private fun HoldingPill(seconds: Int, cancelProgress: Float) {
    val transition = rememberInfiniteTransition(label = "hold")
    val pulse = transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "holdPulse",
    )
    val hint = transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hintShift",
    )
    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = PayraShapes.Pill) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer {
                        scaleX = pulse.value
                        scaleY = pulse.value
                    }
                    .clip(CircleShape)
                    .background(PayraColors.Danger),
            )
            Spacer(Modifier.width(10.dp))
            Text(formatClock(seconds), color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(
                text = if (cancelProgress >= 1f) "Release to cancel" else "‹  Slide to cancel",
                color = if (cancelProgress >= 1f) PayraColors.Danger else PayraColors.Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer {
                    translationX = hint.value * density - cancelProgress * 40.dp.toPx()
                    alpha = 1f - cancelProgress * 0.4f
                },
            )
        }
    }
}

/** After locking: delete, moving waveform (frozen when paused), timer, pause / resume. */
@Composable
private fun LockedPill(
    seconds: Int,
    paused: Boolean,
    canPause: Boolean,
    onDelete: () -> Unit,
    onTogglePause: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "locked")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase",
    )
    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = PayraShapes.Pill) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete recording",
                tint = PayraColors.Danger,
                modifier = Modifier.size(26.dp).bouncyClick(onClick = onDelete, pressedScale = 0.8f),
            )
            Spacer(Modifier.width(12.dp))
            Text(formatClock(seconds), color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Canvas(modifier = Modifier.weight(1f).height(30.dp)) {
                val bars = 22
                val gap = size.width / bars
                val barWidth = gap * 0.55f
                for (i in 0 until bars) {
                    val h = if (paused) 0.35f else 0.25f + 0.75f * ((sin(phase.value + i * 0.55f) + 1f) / 2f)
                    val barHeight = size.height * h
                    drawRoundRect(
                        color = (if (paused) PayraColors.Muted else PayraColors.Primary).copy(alpha = 0.9f),
                        topLeft = Offset(i * gap + (gap - barWidth) / 2f, (size.height - barHeight) / 2f),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                    )
                }
            }
            if (canPause) {
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .bouncyClick(onClick = onTogglePause, pressedScale = 0.85f)
                        .clip(CircleShape)
                        .background(PayraColors.Primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        contentDescription = if (paused) "Resume" else "Pause",
                        tint = PayraColors.Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
