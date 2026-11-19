package com.payra.calling.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.NetState
import com.payra.calling.data.NetworkMonitor

/** Small pill at the top of the screen: "No internet connection" or "Connecting...". */
@Composable
fun ConnectionBanner(modifier: Modifier = Modifier) {
    val state = NetworkMonitor.state
    // keep the last message while the pill slides away
    var shown by remember { mutableStateOf(NetState.Offline) }
    if (state != NetState.Online) shown = state

    AnimatedVisibility(
        visible = state != NetState.Online,
        modifier = modifier,
        enter = slideInVertically(spring(dampingRatio = 0.7f, stiffness = 400f)) { -it * 2 } + fadeIn(tween(160)),
        exit = slideOutVertically(tween(240)) { -it * 2 } + fadeOut(tween(160)),
    ) {
        GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.94f) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (shown == NetState.Offline) {
                    Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = PayraColors.Danger, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("No internet connection", color = PayraColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Connecting", color = PayraColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    BouncingDots(color = PayraColors.Primary, dotSize = 5.dp)
                }
            }
        }
    }
}
