package com.payra.calling.feature.calls

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.popIn

private fun clock(s: Int): String = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

/**
 * The call screen. It only shows the call that CallManager is running, so minimizing keeps the call alive.
 * Voice call: big avatar. Video call: your own camera fills the screen while it rings; when the other person
 * picks up they become the big picture and you shrink to a small corner (tap the small picture to swap).
 */
@Composable
fun CallScreen(onMinimize: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val contact = FakeChatStore.contact(CallManager.contactId)
    val phase = CallManager.phase
    val video = CallManager.video

    var cameraGranted by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        cameraGranted = result[Manifest.permission.CAMERA] == true ||
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    LaunchedEffect(video) {
        if (video && !cameraGranted) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

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
                        ringing = phase == CallPhase.Calling || phase == CallPhase.Ringing || phase == CallPhase.IncomingRinging,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(contact.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    StatusLine(phase = phase, video = false)
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .zIndex(5f),
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

            // Bottom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .zIndex(5f),
            ) {
                when {
                    phase == CallPhase.IncomingRinging -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        RoundCallButton(Icons.Outlined.CallEnd, "Decline", PayraColors.Danger, onClick = { CallManager.decline() })
                        RoundCallButton(
                            icon = if (video) Icons.Outlined.Videocam else Icons.Outlined.Call,
                            label = "Answer",
                            bg = PayraColors.Success,
                            onClick = { CallManager.answer() },
                        )
                    }
                    CallManager.isFinished -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        RoundCallButton(Icons.Outlined.Call, "Call back", PayraColors.Success, onClick = { CallManager.callBack(context) })
                        RoundCallButton(Icons.Outlined.CallEnd, "Close", Color.White.copy(alpha = 0.22f), onClick = { CallManager.close() })
                    }
                    else -> ControlBar(video = video)
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
    val p by animateFloatAsState(
        targetValue = if (remoteBig) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 260f),
        label = "videoSwap",
    )
    val remoteAlpha by animateFloatAsState(targetValue = if (connected) 1f else 0f, animationSpec = tween(400), label = "remoteAlpha")

    val smallW = 110.dp
    val smallH = 150.dp
    val margin = 16.dp
    val smallTop = 96.dp

    // Remote: small -> big as p goes 0 -> 1.  Self: big -> small as p goes 0 -> 1.
    val remoteW = lerp(smallW, maxW, p)
    val remoteH = lerp(smallH, maxH, p)
    val remoteX = lerp(maxW - smallW - margin, 0.dp, p)
    val remoteY = lerp(smallTop, 0.dp, p)
    val remoteCorner = lerp(24.dp, 0.dp, p)

    val selfW = lerp(maxW, smallW, p)
    val selfH = lerp(maxH, smallH, p)
    val selfX = lerp(0.dp, maxW - smallW - margin, p)
    val selfY = lerp(0.dp, smallTop, p)
    val selfCorner = lerp(0.dp, 24.dp, p)

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote picture (a colored stand-in until real video is connected)
        Box(
            modifier = Modifier
                .zIndex(if (remoteBig) 0f else 1f)
                .offset(x = remoteX, y = remoteY)
                .size(width = remoteW, height = remoteH)
                .graphicsLayer { alpha = remoteAlpha }
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

/** A slim glass bar: mute, speaker, camera, flip, end. */
@Composable
private fun ControlBar(video: Boolean) {
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
                onClick = { CallManager.muted = !CallManager.muted },
            )
            ToggleButton(
                icon = Icons.Outlined.VolumeUp,
                description = "Speaker",
                active = CallManager.speaker,
                onClick = { CallManager.speaker = !CallManager.speaker },
            )
            if (video) {
                ToggleButton(
                    icon = if (CallManager.camOff) Icons.Outlined.VideocamOff else Icons.Outlined.Videocam,
                    description = "Camera",
                    active = CallManager.camOff,
                    onClick = { CallManager.camOff = !CallManager.camOff },
                )
                ToggleButton(
                    icon = Icons.Outlined.FlipCameraAndroid,
                    description = "Flip camera",
                    active = false,
                    onClick = { CallManager.frontCamera = !CallManager.frontCamera },
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
private fun RoundCallButton(icon: ImageVector, label: String, bg: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .bouncyClick(onClick = onClick, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}
