package com.payra.calling.feature.calls

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Videocam
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

private data class FakeCall(
    val contactId: String,
    val video: Boolean,
    val time: String,
    val missed: Boolean,
    val outgoing: Boolean,
)

private val FakeCalls = listOf(
    FakeCall("ayesha", false, "9:41 AM", missed = false, outgoing = false),
    FakeCall("rahim", false, "8:12 AM", missed = true, outgoing = false),
    FakeCall("nusrat", true, "Yesterday", missed = false, outgoing = true),
    FakeCall("tanvir", false, "Yesterday", missed = true, outgoing = false),
    FakeCall("farabi", true, "Mon", missed = false, outgoing = true),
)

/** Figma screen "9 Calls". [onCall] = (contactId, incoming, video). */
@Composable
fun CallListScreen(
    contentPadding: PaddingValues,
    onCall: (String, Boolean, Boolean) -> Unit,
) {
    var filter by remember { mutableStateOf("All") }
    val shown = if (filter == "Missed") FakeCalls.filter { it.missed } else FakeCalls

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Calls", style = MaterialTheme.typography.displayLarge, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(0))
        Spacer(Modifier.height(16.dp))

        GlassSurface(modifier = Modifier.enterFromBottom(1), shape = RoundedCornerShape(22.dp)) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("All", "Missed").forEach { label ->
                    val selected = filter == label
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else PayraColors.Muted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) PayraColors.Primary else Color.Transparent)
                            .bouncyClick(onClick = { filter = label })
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.padding(start = 6.dp).enterFromBottom(2)) {
            TextLink(text = "Demo: incoming voice call", onClick = { onCall("ayesha", true, false) })
            Spacer(Modifier.width(16.dp))
            TextLink(text = "Demo: incoming video call", onClick = { onCall("ayesha", true, true) })
        }
        Spacer(Modifier.height(12.dp))

        GlassSurface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = contentPadding.calculateBottomPadding() + 8.dp),
            ) {
                itemsIndexed(shown, key = { index, _ -> index }) { index, call ->
                    val contact = FakeChatStore.contact(call.contactId)
                    Row(
                        modifier = Modifier
                            .enterFromBottom(index + 3)
                            .fillMaxWidth()
                            .bouncyClick(onClick = { onCall(call.contactId, false, call.video) }, pressedScale = 0.97f)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 52.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (call.outgoing) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (call.missed) PayraColors.Danger else PayraColors.Muted,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = (if (call.missed) "Missed" else if (call.outgoing) "Outgoing" else "Incoming") + " · " + call.time,
                                    fontSize = 14.sp,
                                    color = if (call.missed) PayraColors.Danger else PayraColors.Muted,
                                )
                            }
                        }
                        Icon(
                            imageVector = if (call.video) Icons.Outlined.Videocam else Icons.Outlined.Call,
                            contentDescription = "Call back",
                            tint = PayraColors.Primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}
