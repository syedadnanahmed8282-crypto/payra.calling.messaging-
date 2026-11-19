package com.payra.calling.feature.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.SharedPayload
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.EmptyState
import com.payra.calling.designsystem.GlassSearchField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick

/** "Send to...": shown when another app shares something with Payra. Pick the person and it is sent. */
@Composable
fun ShareTargetScreen(payload: SharedPayload, onBack: () -> Unit, onPick: (contactId: String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val people = FakeChatStore.contacts.filter {
        !FakeChatStore.isBlocked(it.id) && (query.isBlank() || it.name.contains(query, ignoreCase = true))
    }
    val summary = buildList {
        if (payload.text.isNotBlank()) add("\"" + payload.text.take(70) + (if (payload.text.length > 70) "..." else "") + "\"")
        if (payload.items.isNotEmpty()) add("${payload.items.size} photo/video" + if (payload.items.size > 1) "s" else "")
        if (payload.files.isNotEmpty()) add("${payload.files.size} file" + if (payload.files.size > 1) "s" else "")
    }.joinToString("  ·  ")

    GlowBackground {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            ScreenHeader(title = "Send to...", onBack = onBack)
            Spacer(Modifier.height(10.dp))
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Text(summary, color = PayraColors.Ink, fontSize = 14.sp, modifier = Modifier.padding(14.dp), maxLines = 3)
            }
            Spacer(Modifier.height(12.dp))
            GlassSearchField(value = query, onValueChange = { query = it }, placeholder = "Search chats")
            Spacer(Modifier.height(12.dp))
            if (people.isEmpty()) {
                EmptyState(icon = Icons.Outlined.Search, title = "No chats found", subtitle = "Try a different name")
            } else {
                GlassSurface(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        items(people, key = { it.id }) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick(onClick = { onPick(contact.id) }, pressedScale = 0.97f)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 48.dp, imageUri = contact.avatarUri)
                                Spacer(Modifier.width(14.dp))
                                Text(contact.name, color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
