package com.payra.calling.feature.calls

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.payra.calling.data.Contact
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.UiPrefs
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.popIn
import kotlinx.coroutines.delay

private fun clock(s: Int): String = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

/** Springs overshoot, so every value used as a size or corner is clamped to 0..1 first and never goes below 0. */
private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp =
    lerp(start, stop, fraction.coerceIn(0f, 1f)).coerceAtLeast(0.dp)

/**
 * The call screen. It only shows the call that CallManager is running, so minimizing keeps the call alive.
 * Incoming call: full-screen caller picture with big Decline / Accept. Voice call: big avatar. Video call: your own
 * camera fills the screen while it rings; when the other person picks up they become the big picture and you shrink
 * to a small corner (tap the small picture to swap). Tap the screen in a connected call to hide / show the controls.
 */
@Composable
fun CallScreen(onMinimize: () -> Unit, onClose: () -> Unit, onMessage: (String) -> Unit) {
    val context = LocalContext.current
    val contact = FakeChatStore.contact(CallManager.contactId)
    val phase = CallManager.phase
    val video = CallManager.video

    var cameraGranted by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var answerAfterPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        cameraGranted = result[Manifest.permission.CAMERA] == true ||
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (answerAfterPermission) {
            answerAfterPermission = false
            // Answer whatever the person chose: without the camera permission the call simply starts with "camera off".
            CallManager.answer()
        }
    }
    LaunchedEffect(video) {
        if (video && !cameraGranted) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    // Controls: tap the screen to hide / show them in a connected call. In video calls they hide by themselves.
    var controlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(phase) { controlsVisible = true }
    LaunchedEffect(controlsVisible, phase, video, interactionTick) {
        if (video && phase == CallPhase.Connected && controlsVisible) {
            delay(4000)
            controlsVisible = false
        }
    }
    val showControls = controlsVisible || phase != CallPhase.Connected

    // When the call is over and has closed itself, leave this screen.
    LaunchedEffect(CallManager.active) {
        if (!CallManager.active) onClose()
    }

    BackHandler {
        if (CallManager.isFinished) {
            CallManager.close()
        } else {
            CallManager.minimized = true
            onMinimize()
        }
    }

    GlowBackground(base = PayraColors.CallDark) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxW = maxWidth
            val maxH = maxHeight

            AnimatedContent(
                targetState = phase == CallPhase.IncomingRinging,
                transitionSpec = { fadeIn(tween(380)) togetherWith fadeOut(tween(260)) },
                label = "incomingToCall",
            ) { ringing ->
                if (ringing) {
                    IncomingCallLayout(
                        contact = contact,
                        video = video,
                        onDecline = { CallManager.decline() },
                        onAccept = {
                            if (video && !cameraGranted) {
                                answerAfterPermission = true
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            } else {
                                CallManager.answer()
                            }
                        },
                        onMessage = {
                            CallManager.decline()
                            CallManager.close()
                            onMessage(contact.id)
                        },
                    )
                } else {
                    ActiveCallLayout(
                        contact = contact,
                        phase = phase,
                        video = video,
                        maxW = maxW,
                        maxH = maxH,
                        cameraGranted = cameraGranted,
                        showControls = showControls,
                        onToggleControls = {
                            if (CallManager.isConnected) controlsVisible = !controlsVisible
                        },
                        onInteract = {
                            controlsVisible = true
                            interactionTick++
                        },
                        onMinimize = onMinimize,
                    )
                }
            }
        }
    }
}

/** Full-screen "someone is calling you" screen. */
@Composable
private fun IncomingCallLayout(
    contact: Contact,
    video: Boolean,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
    onMessage: () -> Unit,
) {
    val calm = UiPrefs.reduceEffects
    val transition = rememberInfiniteTransition(label = "incoming")
    val acceptPulse = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "acceptPulse",
    )
    val statusAlpha = transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "statusAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background: the caller's picture (softly blurred on Android 12+), or a color gradient with a big letter
        if (contact.avatarUri.isNotEmpty()) {
            PayraImage(
                model = contact.avatarUri,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (Build.VERSION.SDK_INT >= 31 && !calm) Modifier.blur(26.dp) else Modifier),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(contact.color.copy(alpha = 0.95f), PayraColors.CallDark))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = contact.name.take(1),
                    color = Color.White.copy(alpha = 0.12f),
                    fontSize = 260.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        // Dark scrim so the text and buttons stay readable on any picture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.88f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))
            PulsingAvatar(
                initial = contact.name.take(1),
                color = contact.color,
                imageUri = contact.avatarUri,
                ringing = !calm,
            )
            Spacer(Modifier.height(22.dp))
            Text(contact.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (video) "Incoming video call" else "Incoming voice call",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 17.sp,
                modifier = Modifier.graphicsLayer { alpha = if (calm) 1f else statusAlpha.value },
            )
            Spacer(Modifier.weight(1f))

            GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.2f, onClick = onMessage) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Message", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RoundCallButton(Icons.Outlined.CallEnd, "Decline", PayraColors.Danger, onClick = onDecline, size = 80)
                RoundCallButton(
                    icon = if (video) Icons.Outlined.Videocam else Icons.Outlined.Call,
                    label = "Accept",
                    bg = PayraColors.Success,
                    onClick = onAccept,
                    size = 80,
                    pulse = if (calm) 1f else acceptPulse.value,
                )
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** Calling / ringing / connected / result screens (voice and video). */
@Composable
private fun ActiveCallLayout(
    contact: Contact,
    phase: CallPhase,
    video: Boolean,
    maxW: Dp,
    maxH: Dp,
    cameraGranted: Boolean,
    showControls: Boolean,
    onToggleControls: () -> Unit,
    onInteract: () -> Unit,
    onMinimize: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { onToggleControls() }) },
    ) {
        if (video) {
            VideoStage(
                maxW = maxW,
                maxH = maxH,
                cameraGranted = cameraGranted,
                contactInitial = contact.name.take(1),
                contactColor = contact.color,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PulsingAvatar(
                    initial = contact.name.take(1),
                    color = contact.color,
                    imageUri = contact.avatarUri,
                    ringing = phase == CallPhase.Calling || phase == CallPhase.Ringing,
                )
                Spacer(Modifier.height(20.dp))
                Text(contact.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatusLine(phase = phase, video = false)
            }
        }

        // Top bar (name and status are part of it in video calls)
        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.TopCenter).zIndex(5f),
            enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { -it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { -it / 2 },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!CallManager.isFinished) {
                    GlassCircleButton(
                        icon = Icons.Outlined.PictureInPictureAlt,
                        contentDescription = "Minimize call",
                        onClick = {
                            CallManager.minimized = true
                            onMinimize()
                        },
                    )
                }
                Spacer(Modifier.weight(1f))
                if (video) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(contact.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        StatusLine(phase = phase, video = true)
                    }
                }
            }
        }

        // Bottom controls
        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(5f),
            enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 2 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                if (CallManager.isFinished) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        RoundCallButton(Icons.Outlined.Call, "Call back", PayraColors.Success, onClick = { CallManager.callBack(context) })
                        RoundCallButton(Icons.Outlined.CallEnd, "Close", Color.White.copy(alpha = 0.22f), onClick = { CallManager.close() })
                    }
                } else {
                    ControlBar(video = video, onInteract = onInteract)
                }
            }
        }
    }
}

@Composable
private fun StatusLine(phase: CallPhase, video: Boolean) {
    val color = Color.White.copy(alpha = 0.8f)
    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
        label = "callStatus",
    ) { p ->
        when (p) {
            CallPhase.IncomingRinging -> Text(if (video) "Incoming video call" else "Incoming voice call", color = color, fontSize = 16.sp)
            CallPhase.Calling -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Calling", color = color, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                BouncingDots(color = color, dotSize = 5.dp)
            }
            CallPhase.Ringing -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ringing", color = color, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                BouncingDots(color = color, dotSize = 5.dp)
            }
            CallPhase.Connected -> Text(clock(CallManager.seconds), color = color, fontSize = 16.sp)
            CallPhase.Busy -> Text("Busy. Try again later", color = Color(0xFFFFB4B6), fontSize = 16.sp)
            CallPhase.NoAnswer -> Text("No answer", color = Color(0xFFFFB4B6), fontSize = 16.sp)
            CallPhase.Declined -> Text("Call declined", color = Color(0xFFFFB4B6), fontSize = 16.sp)
            CallPhase.Ended -> Text(
                if (CallManager.seconds > 0) "Call ended · ${clock(CallManager.seconds)}" else "Call ended",
                color = color,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun PulsingAvatar(initial: String, color: Color, imageUri: String, ringing: Boolean) {
    val transition = rememberInfiniteTransition(label = "callRing")
    val ring1 = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring1",
    )
    val ring1Alpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring1a",
    )
    val ring2 = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1800, delayMillis = 900, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring2",
    )
    val ring2Alpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, delayMillis = 900, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring2a",
    )
    Box(modifier = Modifier.size(150.dp).popIn(), contentAlignment = Alignment.Center) {
        if (ringing) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer { scaleX = ring1.value; scaleY = ring1.value; alpha = ring1Alpha.value }
                    .clip(CircleShape)
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer { scaleX = ring2.value; scaleY = ring2.value; alpha = ring2Alpha.value }
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
        AvatarCircle(
            initial = initial,
            color = color,
            size = 130.dp,
            imageUri = imageUri,
            modifier = Modifier.border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        )
    }
}

/**
 * Two layers: your camera and the other person. Before they answer, your camera is full screen.
 * After they answer they become full screen and you shrink to the corner (springs). Tap the small one to swap.
 */
@Composable
private fun VideoStage(
    maxW: Dp,
    maxH: Dp,
    cameraGranted: Boolean,
    contactInitial: String,
    contactColor: Color,
) {
    val connected = CallManager.isConnected
    val remoteBig = connected && !CallManager.swapped
    val rawP by animateFloatAsState(
        targetValue = if (remoteBig) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 260f),
        label = "videoSwap",
    )
    // The spring can overshoot past 1 (or below 0). Clamp it, or sizes / corner radii become negative and crash.
    val p = rawP.coerceIn(0f, 1f)
    val remoteAlpha by animateFloatAsState(targetValue = if (connected) 1f else 0f, animationSpec = tween(400), label = "remoteAlpha")

    val smallW = 110.dp
    val smallH = 150.dp
    val margin = 16.dp
    val smallTop = 96.dp

    // Remote: small -> big as p goes 0 -> 1.  Self: big -> small as p goes 0 -> 1.
    val remoteW = lerpDp(smallW, maxW, p)
    val remoteH = lerpDp(smallH, maxH, p)
    val remoteX = lerpDp(maxW - smallW - margin, 0.dp, p)
    val remoteY = lerpDp(smallTop, 0.dp, p)
    val remoteCorner = lerpDp(24.dp, 0.dp, p)

    val selfW = lerpDp(maxW, smallW, p)
    val selfH = lerpDp(maxH, smallH, p)
    val selfX = lerpDp(0.dp, maxW - smallW - margin, p)
    val selfY = lerpDp(0.dp, smallTop, p)
    val selfCorner = lerpDp(0.dp, 24.dp, p)

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote picture (a colored stand-in until real video is connected)
        Box(
            modifier = Modifier
                .zIndex(if (remoteBig) 0f else 1f)
                .offset(x = remoteX, y = remoteY)
                .size(width = remoteW, height = remoteH)
                .graphicsLayer { alpha = remoteAlpha.coerceIn(0f, 1f) }
                .clip(RoundedCornerShape(remoteCorner))
                .background(Brush.verticalGradient(listOf(Color(0xFF2B8FAE), Color(0xFF14566B))))
                .then(
                    if (!remoteBig && connected) {
                        Modifier.bouncyClick(onClick = { CallManager.swapped = !CallManager.swapped }, pressedScale = 0.95f)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AvatarCircle(initial = contactInitial, color = contactColor, size = if (remoteBig) 120.dp else 48.dp)
        }

        // Your own camera
        Box(
            modifier = Modifier
                .zIndex(if (remoteBig) 1f else 0f)
                .offset(x = selfX, y = selfY)
                .size(width = selfW, height = selfH)
                .clip(RoundedCornerShape(selfCorner))
                .background(Color(0xFF0A2A33))
                .then(
                    if (remoteBig) {
                        Modifier
                            .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(selfCorner))
                            .bouncyClick(onClick = { CallManager.swapped = !CallManager.swapped }, pressedScale = 0.95f)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (cameraGranted && !CallManager.camOff) {
                SelfCameraPreview(front = CallManager.frontCamera, modifier = Modifier.fillMaxSize())
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.VideocamOff, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(if (remoteBig) 22.dp else 44.dp))
                    if (!remoteBig) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (!cameraGranted) "Camera permission needed" else "Camera off",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

/** A slim glass bar: mute, speaker, camera, flip, end. Every tap restarts the auto-hide timer. */
@Composable
private fun ControlBar(video: Boolean, onInteract: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(40.dp), fillAlpha = 0.14f) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToggleButton(
                icon = if (CallManager.muted) Icons.Outlined.MicOff else Icons.Outlined.MicNone,
                description = if (CallManager.muted) "Unmute" else "Mute",
                active = CallManager.muted,
                onClick = {
                    CallManager.muted = !CallManager.muted
                    onInteract()
                },
            )
            ToggleButton(
                icon = Icons.Outlined.VolumeUp,
                description = "Speaker",
                active = CallManager.speaker,
                onClick = {
                    CallManager.speaker = !CallManager.speaker
                    onInteract()
                },
            )
            if (video) {
                ToggleButton(
                    icon = if (CallManager.camOff) Icons.Outlined.VideocamOff else Icons.Outlined.Videocam,
                    description = "Camera",
                    active = CallManager.camOff,
                    onClick = {
                        CallManager.camOff = !CallManager.camOff
                        onInteract()
                    },
                )
                ToggleButton(
                    icon = Icons.Outlined.FlipCameraAndroid,
                    description = "Flip camera",
                    active = false,
                    onClick = {
                        CallManager.frontCamera = !CallManager.frontCamera
                        onInteract()
                    },
                )
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .bouncyClick(onClick = { CallManager.hangUp() }, pressedScale = 0.88f)
                    .clip(CircleShape)
                    .background(PayraColors.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ToggleButton(icon: ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (active) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.2f),
        animationSpec = tween(180),
        label = "toggleBg",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .bouncyClick(onClick = onClick, pressedScale = 0.88f)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = if (active) PayraColors.CallDark else Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RoundCallButton(
    icon: ImageVector,
    label: String,
    bg: Color,
    onClick: () -> Unit,
    size: Int = 76,
    pulse: Float = 1f,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .bouncyClick(onClick = onClick, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size((size * 0.42f).dp))
        }
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}
