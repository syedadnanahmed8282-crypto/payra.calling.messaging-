package com.payra.calling.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Grey placeholder rows (photo, two lines, time) that gently pulse while a list is loading. */
@Composable
fun SkeletonList(rows: Int = 7, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "skeletonPulse",
    )
    val bone = PayraColors.Ink.copy(alpha = 0.09f)
    Column(modifier = modifier.fillMaxWidth().padding(8.dp).graphicsLayer { alpha = pulse }) {
        repeat(rows) { i ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(bone))
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (i % 2 == 0) 0.5f else 0.38f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(bone),
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (i % 3 == 0) 0.85f else 0.68f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bone),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.width(34.dp).height(10.dp).clip(RoundedCornerShape(5.dp)).background(bone))
            }
        }
    }
}
