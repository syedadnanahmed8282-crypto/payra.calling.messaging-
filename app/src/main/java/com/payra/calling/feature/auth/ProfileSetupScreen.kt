package com.payra.calling.feature.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

/** Figma screen "7 Profile Setup". */
@Composable
fun ProfileSetupScreen(onFinish: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }

    val pulse = rememberInfiniteTransition(label = "badge")
    val badgeScale = pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "badgeScale",
    )

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Set up your profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PayraColors.Ink,
                modifier = Modifier.enterFromBottom(0),
            )
            Text(
                "Add a photo and your name so friends can find you.",
                color = PayraColors.Muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterFromBottom(1),
            )

            Box(modifier = Modifier.size(124.dp).popIn()) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(PayraColors.Glow)
                        .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(56.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .graphicsLayer {
                            scaleX = badgeScale.value
                            scaleY = badgeScale.value
                        }
                        .clip(CircleShape)
                        .background(PayraColors.Primary)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Add photo", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            GlassField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Your name",
                leadingIcon = Icons.Outlined.Person,
                modifier = Modifier.enterFromBottom(3),
            )
            GlassField(
                value = about,
                onValueChange = { about = it },
                placeholder = "About (optional)",
                leadingIcon = Icons.Outlined.ChatBubbleOutline,
                modifier = Modifier.enterFromBottom(4),
            )
            PayraPrimaryButton(text = "Finish", onClick = onFinish, modifier = Modifier.enterFromBottom(5))
            TextLink("Skip for now", onClick = onFinish, modifier = Modifier.enterFromBottom(6))
        }
    }
}
