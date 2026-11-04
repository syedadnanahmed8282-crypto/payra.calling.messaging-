package com.payra.calling.feature.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.preview
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Archived chats list. */
@Composable
fun ArchivedChatsScreen(onBack: () -> Unit, onOpenChat: (String) -> Unit) {
    val archived = FakeChatStore.conversations.filter { it.archived }
    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Archived chats", onBack = onBack)
            Spacer(Modifier.height(14.dp))
            if (archived.isEmpty()) {
                Text("No archived chats", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(8.dp))
            } else {
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        archived.forEach { convo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick(onClick = { onOpenChat(convo.contact.id) }, pressedScale = 0.97f)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AvatarCircle(initial = convo.contact.name.take(1), color = convo.contact.color, size = 52.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(convo.contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                    Text(
                                        text = convo.messages.lastOrNull()?.preview() ?: "No messages yet",
                                        fontSize = 14.sp,
                                        color = PayraColors.Muted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = "Unarchive",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PayraColors.Primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PayraColors.Primary.copy(alpha = 0.12f))
                                        .bouncyClick(onClick = { convo.archived = false }, pressedScale = 0.9f)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
