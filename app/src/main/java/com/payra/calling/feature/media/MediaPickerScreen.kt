package com.payra.calling.feature.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.feature.chats.MediaTile

private val FakeMedia = (0 until 30).map { i ->
    val video = i % 7 == 3
    MediaItem(tone = i % 6, isVideo = video, duration = if (video) "0:${10 + (i * 7) % 49}" else "")
}

/** Figma screen "33 Media Picker": the grid scrolls under the glass bars. */
@Composable
fun MediaPickerScreen(
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    onSend: (List<MediaItem>) -> Unit,
) {
    val chosen = remember { mutableStateListOf<Int>() }

    GlassScaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ScreenHeader(title = "Recents", onBack = onBack)
            }
        },
        bottomBar = {
            if (chosen.isNotEmpty()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(32.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${chosen.size} selected",
                            color = PayraColors.Ink,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .bouncyClick(onClick = { onSend(chosen.map { FakeMedia[it] }) }, pressedScale = 0.88f)
                                .clip(CircleShape)
                                .background(PayraColors.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.navigationBarsPadding())
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 4.dp,
                end = 4.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "camera") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PayraColors.Primary.copy(alpha = 0.15f))
                        .bouncyClick(onClick = onOpenCamera, pressedScale = 0.94f),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = PayraColors.Primary, modifier = Modifier.size(32.dp))
                }
            }
            itemsIndexed(FakeMedia, key = { index, _ -> "m$index" }) { index, item ->
                val isChosen = index in chosen
                val order = chosen.indexOf(index) + 1
                val borderColor by animateColorAsState(
                    targetValue = if (isChosen) PayraColors.Primary else Color.Transparent,
                    animationSpec = tween(160),
                    label = "pickBorder",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .bouncyClick(
                            onClick = { if (isChosen) chosen.remove(index) else chosen.add(index) },
                            pressedScale = 0.95f,
                        )
                        .border(3.dp, borderColor, RoundedCornerShape(12.dp)),
                ) {
                    MediaTile(item, Modifier.fillMaxSize())
                    if (isChosen) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PayraColors.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(order.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
