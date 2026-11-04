package com.payra.calling.feature.chats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.Conversation
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.preview
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassPillField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.IllustrationCircle
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.UnreadBadge
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

/** Figma screens "2 Chats", "38 Empty Chats" and the long-press actions (pin, mute, archive, delete). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    contentPadding: PaddingValues,
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenArchived: () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    val visible = FakeChatStore.conversations.filter { !it.archived }.sortedByDescending { it.pinned }
    val archivedCount = FakeChatStore.conversations.count { it.archived }
    var sheetConvo by remember { mutableStateOf<Conversation?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().enterFromBottom(0),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chats",
                    style = MaterialTheme.typography.displayLarge,
                    color = PayraColors.Ink,
                    modifier = Modifier.weight(1f),
                )
                GlassCircleButton(icon = Icons.Outlined.Add, contentDescription = "New chat", onClick = onNewChat)
            }
            Spacer(Modifier.height(16.dp))
            GlassPillField(
                placeholder = "Search",
                icon = Icons.Outlined.Search,
                modifier = Modifier.enterFromBottom(1),
            )
            Spacer(Modifier.height(16.dp))

            if (visible.isEmpty() && archivedCount == 0) {
                EmptyChats(onNewChat)
            } else {
                GlassSurface(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = contentPadding.calculateBottomPadding() + 8.dp,
                        ),
                    ) {
                        if (archivedCount > 0) {
                            item(key = "archived") {
                                Row(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .bouncyClick(onClick = onOpenArchived, pressedScale = 0.97f)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.Archive, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(14.dp))
                                    Text("Archived", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Primary, modifier = Modifier.weight(1f))
                                    Text(archivedCount.toString(), fontSize = 14.sp, color = PayraColors.Muted)
                                }
                            }
                        }
                        itemsIndexed(visible, key = { _, c -> c.contact.id }) { index, convo ->
                            ChatRow(
                                convo = convo,
                                entranceIndex = index,
                                onClick = { onOpenChat(convo.contact.id) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    sheetConvo = convo
                                    showSheet = true
                                },
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
            BottomSheetOverlay(visible = showSheet, onDismiss = { showSheet = false }) {
                val target = sheetConvo
                if (target != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(initial = target.contact.name.take(1), color = target.contact.color, size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(target.contact.name, color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(14.dp))
                    SheetButton(if (target.pinned) "Unpin chat" else "Pin chat", PayraColors.Ink, onClick = {
                        target.pinned = !target.pinned
                        showSheet = false
                    })
                    Spacer(Modifier.height(8.dp))
                    SheetButton(if (target.muted) "Unmute notifications" else "Mute notifications", PayraColors.Ink, onClick = {
                        target.muted = !target.muted
                        showSheet = false
                    })
                    Spacer(Modifier.height(8.dp))
                    SheetButton("Archive chat", PayraColors.Ink, onClick = {
                        target.archived = true
                        showSheet = false
                    })
                    Spacer(Modifier.height(8.dp))
                    SheetButton("Delete chat", PayraColors.Danger, onClick = {
                        FakeChatStore.deleteChat(target)
                        showSheet = false
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ChatRow(
    convo: Conversation,
    entranceIndex: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val last = convo.messages.lastOrNull()
    Row(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .then(if (entranceIndex < 8) Modifier.enterFromBottom(index = entranceIndex + 2) else Modifier)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarCircle(initial = convo.contact.name.take(1), color = convo.contact.color, size = 52.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = convo.contact.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PayraColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = last?.preview() ?: "No messages yet",
                fontSize = 14.sp,
                color = PayraColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = last?.time ?: "", fontSize = 12.sp, color = PayraColors.Muted)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (convo.muted) {
                    Icon(Icons.Outlined.NotificationsOff, contentDescription = "Muted", tint = PayraColors.Muted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                if (convo.pinned) {
                    Icon(Icons.Outlined.PushPin, contentDescription = "Pinned", tint = PayraColors.Muted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                if (convo.unread > 0) UnreadBadge(convo.unread)
            }
        }
    }
}

@Composable
private fun EmptyChats(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IllustrationCircle(icon = Icons.Outlined.ChatBubbleOutline, modifier = Modifier.popIn())
        Spacer(Modifier.height(14.dp))
        Text("No chats yet", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(1))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Start a conversation with someone from your contacts.",
            fontSize = 15.sp,
            color = PayraColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.enterFromBottom(2),
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .enterFromBottom(3)
                .bouncyClick(onClick = onNewChat)
                .clip(PayraShapes.Pill)
                .background(PayraColors.Primary)
                .padding(horizontal = 30.dp, vertical = 16.dp),
        ) {
            Text("Start new chat", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
