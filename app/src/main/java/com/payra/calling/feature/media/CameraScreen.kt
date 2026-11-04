package com.payra.calling.feature.media

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.feature.chats.MediaTile

/** Figma screen "74 Camera". Fake viewfinder until the real camera (CameraX) is connected. */
@Composable
fun CameraScreen(onClose: () -> Unit, onCapture: (MediaItem) -> Unit) {
    var mode by remember { mutableStateOf("Photo") }
    var flash by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A2A33))) {
        MediaTile(MediaItem(2), Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCircleButton(icon = Icons.Outlined.Close, contentDescription = "Close", onClick = onClose)
                Spacer(Modifier.weight(1f))
                GlassCircleButton(
                    icon = Icons.Outlined.FlashOn,
                    contentDescription = "Flash",
                    onClick = { flash = !flash },
                    tint = if (flash) Color(0xFFE4A93A) else PayraColors.Ink,
                )
            }

            Spacer(Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GlassSurface(shape = RoundedCornerShape(22.dp)) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        listOf("Photo", "Video").forEach { label ->
                            val selected = mode == label
                            Text(
                                text = label,
                                color = if (selected) Color.White else PayraColors.Muted,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) PayraColors.Primary else Color.Transparent)
                                    .bouncyClick(onClick = { mode = label })
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                ) {
                    MediaTile(MediaItem(0), Modifier.fillMaxSize())
                }
                Spacer(Modifier.weight(1f))
                ShutterButton(onClick = { onCapture(MediaItem(3, mode == "Video", if (mode == "Video") "0:08" else "")) })
                Spacer(Modifier.weight(1f))
                GlassCircleButton(icon = Icons.Outlined.FlipCameraAndroid, contentDescription = "Flip camera", onClick = {}, size = 52.dp)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "shutter")
    val ring = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring",
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer {
                    scaleX = ring.value
                    scaleY = ring.value
                }
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(68.dp)
                .bouncyClick(onClick = onClick, pressedScale = 0.86f)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
