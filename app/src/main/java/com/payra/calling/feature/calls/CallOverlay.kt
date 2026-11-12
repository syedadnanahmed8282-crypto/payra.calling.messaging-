package com.payra.calling.feature.calls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import kotlin.math.roundToInt

/**
 * What you see while a call is minimized: a small floating bar (voice call) or a small floating video window
 * (video call). Drag it anywhere, tap it to go back to the call.
 */
@Composable
fun CallOverlay(onExpand: () -> Unit, modifier: Modifier = Modifier) {
    val show = CallManager.active && CallManager.minimized
    AnimatedVisibility(
        visible = show,
        modifier = modifier,
        enter = fadeIn(tween(200)) + scaleIn(spring(dampingRatio = 0.6f, stiffness = 400f), initialScale = 0.7f),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f),
    ) {
        val asWindow = CallManager.video && CallManager.isConnected
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (asWindow) Alignment.TopEnd else Alignment.TopCenter,
        ) {
            if (asWindow) VideoWindow(onExpand = onExpand) else CallBar(onExpand = onExpand)
        }
    }
}

@Composable
private fun CallBar(onExpand: () -> Unit) {
    val contact = FakeChatStore.contact(CallManager.contactId)
    var drag by remember { mutableStateOf(Offset.Zero) }
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

    GlassSurface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 8.dp)
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .pointerInput(Unit) { detectTapGestures(onTap = { onExpand() }) }
            .pointerInput(Unit) { detectDragGestures { change, amount -> change.consume(); drag += amount } },
        shape = RoundedCornerShape(30.dp),
        fillAlpha = 0.85f,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 36.dp, imageUri = contact.avatarUri)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
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
                    .size(36.dp)
                    .bouncyClick(onClick = { CallManager.hangUp() }, pressedScale = 0.85f)
                    .clip(CircleShape)
                    .background(PayraColors.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun VideoWindow(onExpand: () -> Unit) {
    val contact = FakeChatStore.contact(CallManager.contactId)
    var drag by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 8.dp, end = 16.dp)
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .size(width = 120.dp, height = 170.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B8FAE), Color(0xFF14566B))))
            .pointerInput(Unit) { detectTapGestures(onTap = { onExpand() }) }
            .pointerInput(Unit) { detectDragGestures { change, amount -> change.consume(); drag += amount } },
        contentAlignment = Alignment.Center,
    ) {
        AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 52.dp, imageUri = contact.avatarUri)
        Text(
            text = "${CallManager.seconds / 60}:${(CallManager.seconds % 60).toString().padStart(2, '0')}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .size(36.dp)
                .bouncyClick(onClick = { CallManager.hangUp() }, pressedScale = 0.85f)
                .clip(CircleShape)
                .background(PayraColors.Danger),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
