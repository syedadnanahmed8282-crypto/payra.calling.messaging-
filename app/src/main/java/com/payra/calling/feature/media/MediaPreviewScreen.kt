package com.payra.calling.feature.media

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.launch

/**
 * Last look before sending photos / videos: swipe through them, remove ones you do not want,
 * write a caption, then send. Opened after picking from the gallery and after using the camera.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaPreviewScreen(
    initial: List<MediaItem>,
    onBack: () -> Unit,
    onSend: (List<MediaItem>, String) -> Unit,
) {
    val items = remember { mutableStateListOf<MediaItem>().apply { addAll(initial) } }
    var caption by remember { mutableStateOf("") }
    val pager = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size) {
        if (items.isEmpty()) onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A1418))) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            val item = items.getOrNull(page)
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PayraImage(model = item.uri, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    if (item.isVideo) {
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                        }
                        if (item.duration.isNotEmpty()) {
                            Text(
                                text = item.duration,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .statusBarsPadding()
                                    .padding(top = 70.dp, end = 20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
        }

        // top bar: close, counter, remove this one
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassCircleButton(icon = Icons.Outlined.Close, contentDescription = "Close", onClick = onBack, size = 44.dp)
            Spacer(Modifier.weight(1f))
            if (items.size > 1) {
                GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.7f) {
                    Text(
                        text = "${pager.currentPage + 1} / ${items.size}",
                        color = PayraColors.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            GlassCircleButton(
                icon = Icons.Outlined.Delete,
                contentDescription = "Remove this one",
                onClick = {
                    val index = pager.currentPage.coerceIn(0, (items.size - 1).coerceAtLeast(0))
                    if (items.isNotEmpty()) items.removeAt(index)
                },
                size = 44.dp,
            )
        }

        // bottom: thumbnails, caption and send
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().imePadding().padding(bottom = 10.dp),
        ) {
            if (items.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                ) {
                    itemsIndexed(items) { index, item ->
                        val current = pager.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, if (current) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                                .bouncyClick(onClick = { scope.launch { pager.animateScrollToPage(index) } }, pressedScale = 0.92f),
                        ) {
                            PayraImage(model = item.uri, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSurface(modifier = Modifier.weight(1f), shape = PayraShapes.Pill) {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp)) {
                        if (caption.isEmpty()) {
                            Text("Add a caption...", color = PayraColors.Muted, fontSize = 16.sp)
                        }
                        BasicTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            maxLines = 3,
                            textStyle = TextStyle(color = PayraColors.Ink, fontSize = 16.sp),
                            cursorBrush = SolidColor(PayraColors.Primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .bouncyClick(onClick = { if (items.isNotEmpty()) onSend(items.toList(), caption.trim()) }, pressedScale = 0.88f)
                        .clip(CircleShape)
                        .background(PayraColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
