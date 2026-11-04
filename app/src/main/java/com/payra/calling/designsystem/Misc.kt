package com.payra.calling.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
        Spacer(Modifier.width(8.dp))
        GlassSurface(modifier = Modifier.weight(1f), shape = PayraShapes.Pill) {
            Text(
                text = title,
                color = PayraColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
fun LogoTile(size: Dp = 64.dp, corner: Dp = 22.dp, modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier.size(size), shape = RoundedCornerShape(corner)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("P", fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.Bold, color = PayraColors.Primary)
        }
    }
}

@Composable
fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = PayraColors.Danger, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = PayraColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PayraColors.Primary.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = PayraColors.Muted, fontSize = 13.sp)
    }
}

/** Big round icon with two soft rings that breathe (used on permission and empty screens). */
@Composable
fun IllustrationCircle(icon: ImageVector, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "illustration")
    val outer = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "outer",
    )
    val inner = transition.animateFloat(
        initialValue = 1.04f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "inner",
    )
    val bob = transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )

    Box(modifier = modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer { scaleX = outer.value; scaleY = outer.value }
                .clip(CircleShape)
                .background(PayraColors.Primary.copy(alpha = 0.08f)),
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .graphicsLayer { scaleX = inner.value; scaleY = inner.value }
                .clip(CircleShape)
                .background(PayraColors.Primary.copy(alpha = 0.12f)),
        )
        GlassSurface(
            modifier = Modifier.size(92.dp).graphicsLayer { translationY = bob.value * density },
            shape = CircleShape,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PayraColors.Primary,
                modifier = Modifier.align(Alignment.Center).size(40.dp),
            )
        }
    }
}
