package com.payra.calling.feature.chats

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.preview
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSearchField
import androidx.compose.runtime.setValue
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "16 Forward": pick chats; the list scrolls under the glass bars. */
@Composable
fun ForwardScreen(
    fromContactId: String,
    messageIds: List<Long>,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val from = remember(fromContactId) { FakeChatStore.conversationFor(fromContactId) }
    val chosen = remember { mutableStateListOf<String>() }
    var query by remember { androidx.compose.runtime.mutableStateOf("") }
    val people = if (query.isBlank()) FakeChatStore.contacts.toList() else FakeChatStore.contacts.filter { it.name.contains(query, ignoreCase = true) }
    val firstPreview = remember(messageIds) {
        from.messages.firstOrNull { it.id == messageIds.firstOrNull() }?.preview().orEmpty()
    }

    GlassScaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ScreenHeader(title = "Forward to", onBack = onBack)
            }
        },
        bottomBar = {
            SendBar(
                names = chosen.joinToString(", ") { FakeChatStore.contact(it).name },
                enabled = chosen.isNotEmpty(),
                onSend = {
                    FakeChatStore.forward(from, messageIds, chosen.toList())
                    onDone()
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
        ) {
            item {
                Column(modifier = Modifier.enterFromBottom(0)) {
                    GlassSearchField(value = query, onValueChange = { query = it }, placeholder = "Search chats")
                    Spacer(Modifier.height(12.dp))
                    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PayraColors.Primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Shortcut, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (messageIds.size == 1) "Forwarding message" else "Forwarding ${messageIds.size} messages",
                                    color = PayraColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(firstPreview, color = PayraColors.Ink, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "RECENT CHATS",
                        color = PayraColors.Muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 10.dp),
                    )
                }
            }
            item {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        people.forEachIndexed { index, contact ->
                            val isChosen = contact.id in chosen
                            val fill by animateColorAsState(
                                targetValue = if (isChosen) PayraColors.Primary else Color.Transparent,
                                animationSpec = tween(160),
                                label = "chosenFill",
                            )
                            Row(
                                modifier = Modifier
                                    .enterFromBottom(index + 1)
                                    .fillMaxWidth()
                                    .bouncyClick(
                                        onClick = { if (isChosen) chosen.remove(contact.id) else chosen.add(contact.id) },
                                        pressedScale = 0.97f,
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = contact.name,
                                    color = PayraColors.Ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(fill)
                                        .border(2.dp, if (isChosen) PayraColors.Primary else PayraColors.Muted.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SendBar(names: String, enabled: Boolean, onSend: () -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (enabled) names else "Select chats",
                color = if (enabled) PayraColors.Ink else PayraColors.Muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .bouncyClick(onClick = { if (enabled) onSend() }, pressedScale = 0.88f)
                    .clip(CircleShape)
                    .background(if (enabled) PayraColors.Primary else PayraColors.Muted.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}
