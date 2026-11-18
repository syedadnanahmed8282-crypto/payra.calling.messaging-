package com.payra.calling.feature.calls

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The minimized call, floating above every screen (a slim bar for voice calls, a small video window for video calls).
 *  - Drag it anywhere on the screen.
 *  - Push it against the left / right edge and it hides there, leaving a small handle. Tap the handle to bring it back.
 *  - Drag it to the bottom and an "End call" target appears; let go over it to hang up.
 *  - Video window: the first tap makes it a little bigger, the next tap opens the full call screen.
 * The call keeps running the whole time.
 */
@Composable
fun CallOverlay(onExpand: () -> Unit, modifier: Modifier = Modifier) {
    val show = CallManager.active && CallManager.minimized
    AnimatedVisibility(
        visible = show,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(160)),
    ) {
        val asWindow = CallManager.video && CallManager.isConnected
        key(asWindow) { FloatingCall(asWindow = asWindow, onExpand = onExpand) }
    }
}

@Composable
private fun FloatingCall(asWindow: Boolean, onExpand: () -> Unit) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val topInset = WindowInsets.statusBars.getTop(density).toFloat()
    val bottomInset = WindowInsets.navigationBars.getBottom(density).toFloat()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenW = with(density) { maxWidth.toPx() }
        val screenH = with(density) { maxHeight.toPx() }
        val margin = with(density) { 10.dp.toPx() }
        val handle = with(density) { 30.dp.toPx() }
        val edge = with(density) { 12.dp.toPx() }
        val zoneTop = screenH - with(density) { 190.dp.toPx() }

        val x = remember { Animatable(0f) }
        val y = remember { Animatable(0f) }
        var size by remember { mutableStateOf(IntSize.Zero) }
        var ready by remember { mutableStateOf(false) }
        var docked by remember { mutableIntStateOf(0) } // -1 = hidden at the left, 0 = free, 1 = hidden at the right
        var lastSide by remember { mutableIntStateOf(-1) }
        var dockTick by remember { mutableIntStateOf(0) }
        var dragging by remember { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }

        val w = size.width.toFloat()
        val h = size.height.toFloat()
        val overZone = dragging && (y.value + h / 2f) > zoneTop

        // First placement: bar at the top centre, video window at the top right
        LaunchedEffect(size) {
            if (!ready && size.width > 0) {
                x.snapTo(if (asWindow) screenW - w - margin else (screenW - w) / 2f)
                y.snapTo(topInset + with(density) { 8.dp.toPx() })
                ready = true
            }
        }
        // Slide into the side (hidden, only the handle shows)
        LaunchedEffect(docked, dockTick, size.width) {
            if (!ready) return@LaunchedEffect
            if (docked != 0) lastSide = docked
            when (docked) {
                -1 -> x.animateTo(-(w - handle), spring(dampingRatio = 0.78f, stiffness = 380f))
                1 -> x.animateTo(screenW - handle, spring(dampingRatio = 0.78f, stiffness = 380f))
                else -> Unit
            }
        }
        // When the window grows or shrinks keep it on the screen
        LaunchedEffect(expanded, size) {
            if (ready && docked == 0 && !dragging) {
                x.animateTo(x.value.coerceIn(margin, (screenW - w - margin).coerceAtLeast(margin)))
                y.animateTo(y.value.coerceIn(topInset, (screenH - h).coerceAtLeast(topInset)))
            }
        }
        // The bigger window shrinks back by itself
        LaunchedEffect(expanded) {
            if (expanded) {
                delay(4500)
                expanded = false
            }
        }
        LaunchedEffect(overZone) {
            if (overZone) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        fun finishDrag() {
            dragging = false
            if (y.value + h / 2f > zoneTop) {
                CallManager.hangUp()
                return
            }
            val left = x.value
            val right = left + w
            docked = when {
                left <= edge -> -1
                right >= screenW - edge -> 1
                else -> 0
            }
            dockTick++
        }

        val pressScale by animateFloatAsState(
            targetValue = if (overZone) 0.8f else 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
            label = "overZone",
        )

        // "End call" target at the bottom while dragging
        AnimatedVisibility(
            visible = dragging,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = with(density) { bottomInset.toDp() } + 36.dp),
            enter = fadeIn(tween(180)) + slideInVertically(spring(dampingRatio = 0.7f, stiffness = 400f)) { it } + scaleIn(initialScale = 0.6f),
            exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it } + scaleOut(targetScale = 0.6f),
        ) {
            val zoneScale by animateFloatAsState(
                targetValue = if (overZone) 1.3f else 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                label = "zoneScale",
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = zoneScale
                            scaleY = zoneScale
                        }
                        .clip(CircleShape)
                        .background(if (overZone) PayraColors.Danger else PayraColors.Danger.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Drop here to end call",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // The floating bar / window itself
        Box(
            modifier = Modifier
                .offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }
                .graphicsLayer {
                    alpha = if (ready) 1f else 0f
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .onSizeChanged { size = it }
                .pointerInput(docked, expanded, size, asWindow) {
                    detectTapGestures(
                        onTap = {
                            when {
                                docked != 0 -> {
                                    // Bring it back out of the side
                                    val side = docked
                                    docked = 0
                                    scope.launch {
                                        x.animateTo(
                                            if (side == -1) margin else screenW - w - margin,
                                            spring(dampingRatio = 0.7f, stiffness = 400f),
                                        )
                                    }
                                }
                                asWindow && !expanded -> expanded = true
                                else -> onExpand()
                            }
                        },
                    )
                }
                .pointerInput(size, screenW, screenH) {
                    detectDragGestures(
                        onDragStart = {
                            dragging = true
                            docked = 0
                        },
                        onDragEnd = { finishDrag() },
                        onDragCancel = { finishDrag() },
                    ) { change, amount ->
                        change.consume()
                        scope.launch {
                            x.snapTo((x.value + amount.x).coerceIn(-(w - handle), screenW - handle))
                            y.snapTo((y.value + amount.y).coerceIn(topInset, (screenH - h).coerceAtLeast(topInset)))
                        }
                    }
                },
        ) {
            if (asWindow) WindowContent(expanded = expanded) else BarContent()

            // The little handle you see while it is hidden at a side
            Box(modifier = Modifier.matchParentSize()) {
                AnimatedVisibility(
                    visible = docked != 0,
                    modifier = Modifier
                        .align(if (lastSide == -1) Alignment.CenterEnd else Alignment.CenterStart)
                        .width(30.dp)
                        .fillMaxHeight(),
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(120)),
                ) {
                    DockHandle(pointsRight = lastSide == -1)
                }
            }
        }
    }
}

@Composable
private fun DockHandle(pointsRight: Boolean) {
    val transition = rememberInfiniteTransition(label = "handle")
    val nudge = transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nudge",
    )
    val shape = if (pointsRight) {
        RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(PayraColors.CallDark.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(PayraColors.Success))
            Spacer(Modifier.height(4.dp))
            Icon(
                imageVector = if (pointsRight) Icons.Outlined.ChevronRight else Icons.Outlined.ChevronLeft,
                contentDescription = "Show call",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { translationX = if (pointsRight) nudge.value else -nudge.value },
            )
        }
    }
}

@Composable
private fun BarContent() {
    val contact = FakeChatStore.contact(CallManager.contactId)
    val transition = rememberInfiniteTransition(label = "barDot")
    val pulse = transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "barPulse",
    )
    val status = when (CallManager.phase) {
        CallPhase.Connected -> "${CallManager.seconds / 60}:${(CallManager.seconds % 60).toString().padStart(2, '0')}"
        CallPhase.Calling -> "Calling..."
        CallPhase.Ringing -> "Ringing..."
        CallPhase.IncomingRinging -> "Incoming call"
        CallPhase.Busy -> "Busy"
        CallPhase.NoAnswer -> "No answer"
        CallPhase.Declined -> "Declined"
        CallPhase.Ended -> "Call ended"
    }
    GlassSurface(shape = RoundedCornerShape(30.dp), fillAlpha = 0.88f) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 38.dp, imageUri = contact.avatarUri)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = pulse.value
                        scaleY = pulse.value
                    }
                    .clip(CircleShape)
                    .background(PayraColors.Success),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(contact.name, color = PayraColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(status, color = PayraColors.Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .bouncyClick(onClick = { CallManager.hangUp() }, pressedScale = 0.85f)
                    .clip(CircleShape)
                    .background(PayraColors.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(19.dp))
            }
        }
    }
}

/** The floating video window shows the picture that was BIG on the call screen when it was minimized. */
@Composable
private fun WindowContent(expanded: Boolean) {
    val context = LocalContext.current
    val contact = FakeChatStore.contact(CallManager.contactId)
    val windowW by animateDpAsState(if (expanded) 172.dp else 118.dp, spring(dampingRatio = 0.8f, stiffness = 380f), label = "winW")
    val windowH by animateDpAsState(if (expanded) 240.dp else 168.dp, spring(dampingRatio = 0.8f, stiffness = 380f), label = "winH")
    val cameraGranted = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val showRemote = !CallManager.swapped

    Box(
        modifier = Modifier
            .size(windowW.coerceAtLeast(1.dp), windowH.coerceAtLeast(1.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B8FAE), Color(0xFF14566B)))),
        contentAlignment = Alignment.Center,
    ) {
        if (showRemote) {
            if (contact.avatarUri.isNotEmpty()) {
                PayraImage(model = contact.avatarUri, modifier = Modifier.fillMaxSize())
            } else {
                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = if (expanded) 80.dp else 56.dp)
            }
        } else if (cameraGranted && !CallManager.camOff) {
            SelfCameraPreview(front = CallManager.frontCamera, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Outlined.VideocamOff, contentDescription = null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(36.dp))
        }
        Text(
            text = "${CallManager.seconds / 60}:${(CallManager.seconds % 60).toString().padStart(2, '0')}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 7.dp, vertical = 3.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .size(40.dp)
                .bouncyClick(onClick = { CallManager.hangUp() }, pressedScale = 0.85f)
                .clip(CircleShape)
                .background(PayraColors.Danger),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
