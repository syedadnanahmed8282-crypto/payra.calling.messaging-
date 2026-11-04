package com.payra.calling.feature.chats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.preview
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "37 Chat Search": matches are highlighted in teal. */
@Composable
fun ChatSearchScreen(contactId: String, onBack: () -> Unit) {
    val convo = remember(contactId) { FakeChatStore.conversationFor(contactId) }
    var query by remember { mutableStateOf("") }
    val results = if (query.isBlank()) {
        emptyList()
    } else {
        convo.messages.filter { !it.deleted && it.preview().contains(query, ignoreCase = true) }
    }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Search in chat", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(14.dp))
            GlassField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search messages",
                leadingIcon = Icons.Outlined.Search,
                modifier = Modifier.enterFromBottom(1),
            )
            Spacer(Modifier.height(14.dp))
            if (query.isNotBlank()) {
                Text(
                    text = "${results.size} MESSAGES FOUND",
                    color = PayraColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                )
            }
            if (results.isNotEmpty()) {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        results.forEach { message ->
                            val text = message.preview()
                            val start = text.indexOf(query, ignoreCase = true)
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                AvatarCircle(
                                    initial = if (message.sent) "Y" else convo.contact.name.take(1),
                                    color = if (message.sent) PayraColors.Primary else convo.contact.color,
                                    size = 44.dp,
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = if (message.sent) "You" else convo.contact.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PayraColors.Ink,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(message.time, fontSize = 12.sp, color = PayraColors.Muted)
                                    }
                                    Text(
                                        text = buildAnnotatedString {
                                            if (start >= 0) {
                                                append(text.substring(0, start))
                                                withStyle(SpanStyle(color = PayraColors.Primary, fontWeight = FontWeight.Bold)) {
                                                    append(text.substring(start, start + query.length))
                                                }
                                                append(text.substring(start + query.length))
                                            } else {
                                                append(text)
                                            }
                                        },
                                        fontSize = 14.sp,
                                        color = PayraColors.Muted,
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (query.isNotBlank()) {
                Text("No messages found", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
