package com.payra.calling.feature.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MessageKind
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.feature.chats.MediaTile

/** Figma screen "31 Shared Media": Media, Files and Links tabs; the grid scrolls under the glass bars. */
@Composable
fun SharedMediaScreen(contactId: String, onBack: () -> Unit, onOpenMedia: (MediaItem) -> Unit) {
    val convo = remember(contactId) { FakeChatStore.conversationFor(contactId) }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Media", "Files", "Links")

    val media = convo.messages
        .filter { !it.deleted && (it.kind == MessageKind.Image || it.kind == MessageKind.Video) }
        .flatMap { it.media }
        .ifEmpty { (0 until 9).map { MediaItem(it % 6, it % 5 == 2, if (it % 5 == 2) "0:1${it}" else "") } }
    val files = convo.messages.filter { !it.deleted && it.kind == MessageKind.File }

    GlassScaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ScreenHeader(title = "Media and files", onBack = onBack)
                Spacer(Modifier.height(10.dp))
                GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                    BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                        val tabWidth = maxWidth / tabs.size
                        val pillX by animateDpAsState(
                            targetValue = tabWidth * tab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                            label = "mediaTab",
                        )
                        Box(
                            modifier = Modifier
                                .offset(x = pillX)
                                .width(tabWidth)
                                .height(40.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PayraColors.Primary),
                        )
                        Row {
                            tabs.forEachIndexed { index, label ->
                                Box(
                                    modifier = Modifier
                                        .width(tabWidth)
                                        .height(40.dp)
                                        .bouncyClick(onClick = { tab = index }, pressedScale = 0.94f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (tab == index) Color.White else PayraColors.Muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) },
            label = "sharedTabs",
        ) { current ->
            when (current) {
                0 -> LazyVerticalGrid(
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
                    itemsIndexed(media, key = { index, _ -> "s$index" }) { _, item ->
                        MediaTile(
                            item = item,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                            onClick = { onOpenMedia(item) },
                        )
                    }
                }
                1 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding() + 12.dp, start = 16.dp, end = 16.dp),
                ) {
                    if (files.isEmpty()) {
                        Text("No files yet", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(8.dp))
                    } else {
                        GlassSurface(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                files.forEach { file ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(PayraColors.Danger.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = PayraColors.Danger, modifier = Modifier.size(22.dp))
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(file.fileName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                            Text(file.fileInfo, fontSize = 12.sp, color = PayraColors.Muted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding() + 12.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text("No links shared yet", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
