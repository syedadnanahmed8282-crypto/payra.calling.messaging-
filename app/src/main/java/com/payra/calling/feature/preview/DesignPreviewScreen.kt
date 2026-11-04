package com.payra.calling.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassPillField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlassTabBar
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.TabItem
import com.payra.calling.designsystem.UnreadBadge

/** Temporary screen: shows the design system on the phone so it can be compared with Figma. */
@Composable
fun DesignPreviewScreen() {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Chats", Icons.Outlined.ChatBubbleOutline),
        TabItem("Calls", Icons.Outlined.Call),
        TabItem("Contacts", Icons.Outlined.People),
        TabItem("Settings", Icons.Outlined.Tune),
    )

    GlowBackground {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                Text("Chats", style = MaterialTheme.typography.displayLarge, color = PayraColors.Ink)
                GlassPillField("Search", Icons.Outlined.Search)

                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SampleRow("Ayesha", "Sent a voice message", "2m", 2, PayraColors.Primary)
                        SampleRow("Rahim Bhai", "See you tomorrow!", "18m", 0, Color(0xFFF28B66))
                        SampleRow("Nusrat", "Thanks a lot", "Yesterday", 0, Color(0xFFE4A93A))
                        SampleRow("Tanvir", "Missed voice call", "Mon", 1, Color(0xFF3FA796))
                    }
                }

                PayraPrimaryButton(text = "Continue", onClick = {})

                Text("Colors", style = MaterialTheme.typography.titleMedium, color = PayraColors.Ink)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Swatch(PayraColors.Primary)
                    Swatch(PayraColors.Ink)
                    Swatch(PayraColors.Muted)
                    Swatch(PayraColors.SurfaceTint)
                    Swatch(PayraColors.AccentLight)
                    Swatch(PayraColors.Danger)
                    Swatch(PayraColors.Success)
                }
                Spacer(Modifier.height(8.dp))
            }

            GlassTabBar(
                items = tabs,
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), shape),
    )
}

@Composable
private fun SampleRow(name: String, preview: String, time: String, unread: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarCircle(initial = name.take(1), color = color, size = 52.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PayraColors.Ink)
            Text(preview, fontSize = 14.sp, color = PayraColors.Muted, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, fontSize = 12.sp, color = PayraColors.Muted)
            if (unread > 0) {
                Spacer(Modifier.height(6.dp))
                UnreadBadge(unread)
            }
        }
    }
}
