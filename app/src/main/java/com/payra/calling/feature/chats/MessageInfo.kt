package com.payra.calling.feature.chats

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.ChatMessage
import com.payra.calling.data.MessageStatus
import com.payra.calling.data.preview
import com.payra.calling.designsystem.PayraColors

/** "Message info": when a message was sent, delivered and read (exact times come with the server). */
@Composable
fun MessageInfoContent(message: ChatMessage, contactName: String) {
    Text("Message info", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(message.preview(), color = PayraColors.Muted, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Spacer(Modifier.height(12.dp))
    if (message.sent) {
        val delivered = message.status == MessageStatus.Delivered || message.status == MessageStatus.Read
        val read = message.status == MessageStatus.Read
        InfoRow(Icons.Outlined.Check, "Sent", message.time, true)
        InfoRow(Icons.Outlined.DoneAll, "Delivered to $contactName", if (delivered) message.time else "Not yet", delivered)
        InfoRow(Icons.Outlined.DoneAll, "Read by $contactName", if (read) message.time else "Not yet", read, tint = PayraColors.Primary)
    } else {
        InfoRow(Icons.Outlined.Schedule, "Received from $contactName", message.time, true)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, done: Boolean, tint: Color = PayraColors.Muted) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = if (done) tint else PayraColors.Muted.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = PayraColors.Ink, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, color = PayraColors.Muted, fontSize = 14.sp)
    }
}
