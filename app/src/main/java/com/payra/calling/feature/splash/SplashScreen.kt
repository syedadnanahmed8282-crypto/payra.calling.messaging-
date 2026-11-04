package com.payra.calling.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Logo springs in, a soft ring breathes behind it, the tagline fades up, loading dots bounce. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val tagAlpha = remember { Animatable(0f) }
    val tagLift = remember { Animatable(18f) }

    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 170f)) }
        launch { logoAlpha.animateTo(1f, tween(450)) }
        delay(420)
        launch { tagAlpha.animateTo(1f, tween(500)) }
        launch { tagLift.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f)) }
        delay(1250)
        onFinished()
    }

    val ring = rememberInfiniteTransition(label = "splashRing")
    val ringScale = ring.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ringScale",
    )
    val ringAlpha = ring.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ringAlpha",
    )

    GlowBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = ringScale.value
                            scaleY = ringScale.value
                            alpha = ringAlpha.value * logoAlpha.value
                        }
                        .clip(CircleShape)
                        .background(PayraColors.Primary),
                )
                GlassSurface(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                        },
                    shape = RoundedCornerShape(40.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("P", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = PayraColors.Primary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Chat. Call. Connect.",
                color = PayraColors.Muted,
                fontSize = 16.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = tagAlpha.value
                    translationY = tagLift.value * density
                },
            )
            Spacer(Modifier.height(36.dp))
            BouncingDots(color = PayraColors.Primary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
