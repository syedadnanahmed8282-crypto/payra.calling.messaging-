package com.payra.calling.feature.calls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.MicOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.Contact
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.popIn
import kotlinx.coroutines.delay

enum class CallUiState { Ringing, Calling, Connected }

private fun formatSeconds(s: Int): String = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

/**
 * Figma screens 4, 5, 8, 18: outgoing call ("Calling..."), incoming call (Decline / Answer),
 * connected audio call and connected video call. Fake call: no real audio or video yet.
 */
@Composable
fun CallScreen(
    contactId: String,
    incoming: Boolean,
    video: Boolean,
    onEnd: () -> Unit,
) {
    val contact = remember(contactId) { FakeChatStore.contact(contactId) }
    var state by remember { mutableStateOf(if (incoming) CallUiState.Ringing else CallUiState.Calling) }
    var elapsed by remember { mutableIntStateOf(0) }
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    var camOff by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    val endCall: () -> Unit = { if (state == CallUiState.Connected) ended = true else onEnd() }

    LaunchedEffect(state) {
        if (state == CallUiState.Calling) {
            delay(2200)
            state = CallUiState.Connected
        }
        if (state == CallUiState.Connected) {
            while (true) {
                delay(1000)
                elapsed++
            }
        }
    }

    if (ended) {
        CallEnded(
            contact = contact,
            seconds = elapsed,
            onClose = onEnd,
            onCallBack = {
                ended = false
                elapsed = 0
                state = CallUiState.Calling
            },
        )
        return
    }

    GlowBackground(base = PayraColors.CallDark) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            if (video && state == CallUiState.Connected) {
                VideoStage(contact = contact, camOff = camOff)
            } else {
                Spacer(Modifier.height(16.dp))
                PulsingAvatar(contact)
            }
            Spacer(Modifier.height(20.dp))
            Text(contact.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
                label = "callStatus",
            ) { s ->
                when (s) {
                    CallUiState.Ringing -> Text(
                        text = if (video) "Incoming video call" else "Incoming voice call",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16.sp,
                    )
                    CallUiState.Calling -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calling", color = Color.White.copy(alpha = 0.75f), fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        BouncingDots(color = Color.White.copy(alpha = 0.75f), dotSize = 5.dp)
                    }
                    CallUiState.Connected -> Text(
                        text = formatSeconds(elapsed),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16.sp,
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            if (state == CallUiState.Ringing) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RoundCallButton(Icons.Outlined.CallEnd, "Decline", PayraColors.Danger, onEnd)
                    RoundCallButton(Icons.Outlined.Call, "Answer", PayraColors.Success, onClick = { state = CallUiState.Connected })
                }
            } else {
                GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(44.dp), fillAlpha = 0.14f) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ToggleCallButton(if (muted) Icons.Outlined.MicOff else Icons.Outlined.MicNone, "Mute", muted) { muted = !muted }
                        ToggleCallButton(Icons.Outlined.VolumeUp, "Speaker", speaker) { speaker = !speaker }
                        if (video) {
                            ToggleCallButton(if (camOff) Icons.Outlined.VideocamOff else Icons.Outlined.Videocam, "Camera", camOff) { camOff = !camOff }
                        }
                        RoundCallButton(Icons.Outlined.CallEnd, "End", PayraColors.Danger, endCall, size = 64)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PulsingAvatar(contact: Contact) {
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
        AvatarCircle(
            initial = contact.name.take(1),
            color = contact.color,
            size = 130.dp,
            modifier = Modifier.border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        )
    }
}

@Composable
private fun VideoStage(contact: Contact, camOff: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF2B8FAE)),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 110.dp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(width = 96.dp, height = 132.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (camOff) Color(0xFF0A2A33) else Color(0xFF1B6F8A))
                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AvatarCircle(initial = "Y", color = PayraColors.Primary, size = 44.dp)
        }
    }
}

@Composable
private fun RoundCallButton(icon: ImageVector, label: String, bg: Color, onClick: () -> Unit, size: Int = 80) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .bouncyClick(onClick = onClick, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size((size * 0.42f).dp))
        }
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun ToggleCallButton(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .bouncyClick(onClick = onClick, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(if (active) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = if (active) PayraColors.CallDark else Color.White, modifier = Modifier.size(28.dp))
        }
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}


/** Figma screen "Call Ended": shows how long the call lasted, with Call back and Close. */
@Composable
private fun CallEnded(contact: Contact, seconds: Int, onClose: () -> Unit, onCallBack: () -> Unit) {
    GlowBackground(base = PayraColors.CallDark) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))
            AvatarCircle(
                initial = contact.name.take(1),
                color = contact.color,
                size = 120.dp,
                modifier = Modifier.popIn().border(4.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            )
            Spacer(Modifier.height(20.dp))
            Text(contact.name, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Call ended", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
            Text(formatSeconds(seconds), color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RoundCallButton(Icons.Outlined.Call, "Call back", PayraColors.Success, onCallBack)
                RoundCallButton(Icons.Outlined.CallEnd, "Close", Color.White.copy(alpha = 0.22f), onClose)
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
