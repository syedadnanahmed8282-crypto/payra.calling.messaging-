package com.payra.calling.feature.newchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.Contact
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSearchField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "36 New Chat". */
@Composable
fun NewChatScreen(onBack: () -> Unit, onOpenChat: (String) -> Unit, onNewContact: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val people = if (query.isBlank()) FakeChatStore.contacts.toList() else FakeChatStore.contacts.filter { it.name.contains(query, ignoreCase = true) }
    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "New chat", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(16.dp))
            GlassSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search name or number",
                modifier = Modifier.enterFromBottom(1),
            )
            Spacer(Modifier.height(16.dp))

            GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    ActionRow(Icons.Outlined.PersonAdd, "New contact", onNewContact)
                    ActionRow(Icons.Outlined.Share, "Invite friends") { com.payra.calling.designsystem.shareInvite(context) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "CONTACTS ON THE APP",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PayraColors.Muted,
                modifier = Modifier.padding(start = 12.dp, bottom = 10.dp).enterFromBottom(3),
            )
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    people.forEachIndexed { index, contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { onOpenChat(contact.id) },
                            modifier = Modifier.enterFromBottom(4 + index),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick, pressedScale = 0.97f)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PayraColors.Primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick, pressedScale = 0.97f)
            .clip(RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
            Text(contact.about, fontSize = 14.sp, color = PayraColors.Muted)
        }
    }
}
