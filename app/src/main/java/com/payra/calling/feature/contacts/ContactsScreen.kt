package com.payra.calling.feature.contacts

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSearchField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "10 Contacts". */
@Composable
fun ContactsScreen(
    contentPadding: PaddingValues,
    onOpenChat: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    onNewContact: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val people = if (query.isBlank()) FakeChatStore.contacts.toList() else FakeChatStore.contacts.filter { it.name.contains(query, ignoreCase = true) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Contacts", style = MaterialTheme.typography.displayLarge, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(0))
        Spacer(Modifier.height(16.dp))
        GlassSearchField(value = query, onValueChange = { query = it }, placeholder = "Search contacts", modifier = Modifier.enterFromBottom(1))
        Spacer(Modifier.height(12.dp))

        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                listOf(
                    Triple("New contact", Icons.Outlined.PersonAdd, onNewContact),
                    Triple("Invite friends", Icons.Outlined.Share, { com.payra.calling.designsystem.shareInvite(context) }),
                ).forEach { (label, icon, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().bouncyClick(onClick = action, pressedScale = 0.97f).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "ON THE APP",
            color = PayraColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 10.dp).enterFromBottom(3),
        )
        GlassSurface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = contentPadding.calculateBottomPadding() + 8.dp),
            ) {
                itemsIndexed(people, key = { _, c -> c.id }) { index, contact ->
                    Row(
                        modifier = Modifier
                            .enterFromBottom(index + 4)
                            .fillMaxWidth()
                            .bouncyClick(onClick = { onViewProfile(contact.id) }, pressedScale = 0.97f)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 52.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                            Text(contact.about, fontSize = 14.sp, color = PayraColors.Muted)
                        }
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Message",
                            tint = PayraColors.Primary,
                            modifier = Modifier.size(22.dp).bouncyClick(onClick = { onOpenChat(contact.id) }, pressedScale = 0.75f),
                        )
                    }
                }
            }
        }
    }
}
