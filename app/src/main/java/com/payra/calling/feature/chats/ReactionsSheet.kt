package com.payra.calling.feature.chats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.Contact
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.Reaction
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick

/** "Who reacted": every reaction on a message with the person who gave it. Tap your own to remove it. */
@Composable
fun ReactionsList(reactions: List<Reaction>, contact: Contact, onRemoveMine: (String) -> Unit) {
    val me = FakeChatStore.me
    val total = reactions.sumOf { it.count }
    Text("Reactions  ·  $total", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    reactions.forEach { r ->
        if (r.mine) {
            Row(
                modifier = Modifier.fillMaxWidth().bouncyClick(onClick = { onRemoveMine(r.emoji) }, pressedScale = 0.97f).padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarCircle(initial = me.name.take(1), color = me.color, size = 44.dp, imageUri = me.avatarUri)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("You", color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tap to remove", color = PayraColors.Muted, fontSize = 12.sp)
                }
                Text(r.emoji, fontSize = 26.sp)
            }
        }
        repeat(r.count - if (r.mine) 1 else 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 44.dp, imageUri = contact.avatarUri)
                Spacer(Modifier.width(12.dp))
                Text(contact.name, color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(r.emoji, fontSize = 26.sp)
            }
        }
    }
}
