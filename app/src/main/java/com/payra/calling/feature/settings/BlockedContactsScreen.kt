package com.payra.calling.feature.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "42 Blocked Contacts". */
@Composable
fun BlockedContactsScreen(onBack: () -> Unit) {
    val blockedIds = FakeChatStore.blocked.toList()
    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Blocked contacts", onBack = onBack)
            Spacer(Modifier.height(14.dp))
            Text(
                "Blocked contacts cannot message or call you. They are not told that you blocked them.",
                color = PayraColors.Muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp).enterFromBottom(0),
            )
            Spacer(Modifier.height(14.dp))
            if (blockedIds.isEmpty()) {
                Text("No blocked contacts", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(8.dp))
            } else {
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        blockedIds.forEach { id ->
                            val contact = FakeChatStore.contact(id)
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                    Text("+880 1XXX XXX 482", fontSize = 13.sp, color = PayraColors.Muted)
                                }
                                Text(
                                    text = "Unblock",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PayraColors.Primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PayraColors.Primary.copy(alpha = 0.12f))
                                        .bouncyClick(onClick = { FakeChatStore.blocked.remove(id) }, pressedScale = 0.9f)
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
