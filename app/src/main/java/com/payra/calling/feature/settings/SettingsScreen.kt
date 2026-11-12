package com.payra.calling.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Storage
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.shareInvite
import com.payra.calling.feature.chats.BottomSheetOverlay
import com.payra.calling.feature.chats.SheetButton

object SettingsRoutes {
    const val BLOCKED = "blocked"
    const val ACCOUNT = "account"
    const val PRIVACY = "privacy"
    const val NOTIFICATIONS = "notifications"
    const val CHATS = "chats"
    const val STORAGE = "storage"
    const val APPEARANCE = "appearance"
    const val LANGUAGE = "language"
    const val HELP = "help"
    const val ABOUT = "about"
    const val PASSWORD = "password"
    const val GMAIL = "gmail"
}

private data class SettingItem(val icon: ImageVector, val label: String, val color: Color, val onClick: () -> Unit)

/** Figma screen "11 Settings". */
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onMyProfile: () -> Unit,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val me = FakeChatStore.me
    var confirmLogout by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Settings", style = MaterialTheme.typography.displayLarge, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(0))
            Spacer(Modifier.height(16.dp))

            GlassSurface(
                modifier = Modifier.fillMaxWidth().enterFromBottom(1),
                onClick = onMyProfile,
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AvatarCircle(initial = me.name.take(1), color = me.color, size = 64.dp, imageUri = me.avatarUri)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(me.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                        Text("+880 1XXX XXXXXX", fontSize = 14.sp, color = PayraColors.Muted)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            SettingsGroup(
                index = 2,
                items = listOf(
                    SettingItem(Icons.Outlined.AccountCircle, "Account", PayraColors.Primary) { onOpen(SettingsRoutes.ACCOUNT) },
                    SettingItem(Icons.Outlined.Lock, "Privacy", Color(0xFF7B6FD6)) { onOpen(SettingsRoutes.PRIVACY) },
                    SettingItem(Icons.Outlined.Notifications, "Notifications", Color(0xFFF28B66)) { onOpen(SettingsRoutes.NOTIFICATIONS) },
                    SettingItem(Icons.Outlined.ChatBubbleOutline, "Chats", Color(0xFF3FA796)) { onOpen(SettingsRoutes.CHATS) },
                    SettingItem(Icons.Outlined.Storage, "Storage and data", Color(0xFF2B8FAE)) { onOpen(SettingsRoutes.STORAGE) },
                    SettingItem(Icons.Outlined.Palette, "Appearance", Color(0xFFD6457B)) { onOpen(SettingsRoutes.APPEARANCE) },
                    SettingItem(Icons.Outlined.Language, "Language", Color(0xFF3FA796)) { onOpen(SettingsRoutes.LANGUAGE) },
                ),
            )
            Spacer(Modifier.height(12.dp))
            SettingsGroup(
                index = 3,
                items = listOf(
                    SettingItem(Icons.Outlined.PersonAdd, "Invite friends", PayraColors.Primary) { shareInvite(context) },
                    SettingItem(Icons.Outlined.QuestionMark, "Help", Color(0xFFE4A93A)) { onOpen(SettingsRoutes.HELP) },
                    SettingItem(Icons.Outlined.Info, "About", Color(0xFF7B6FD6)) { onOpen(SettingsRoutes.ABOUT) },
                ),
            )
            Spacer(Modifier.height(12.dp))
            SettingsGroup(
                index = 4,
                items = listOf(SettingItem(Icons.Outlined.PowerSettingsNew, "Log out", PayraColors.Danger) { confirmLogout = true }),
                danger = true,
            )
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Version 0.1.0", color = PayraColors.Muted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 20.dp))
        }
        Box(modifier = Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
            BottomSheetOverlay(visible = confirmLogout, onDismiss = { confirmLogout = false }) {
                Text("Log out?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("You can log back in any time with your phone number and password.", color = PayraColors.Muted, fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                SheetButton("Log out", PayraColors.Danger, onClick = {
                    confirmLogout = false
                    onLogout()
                })
                Spacer(Modifier.height(10.dp))
                SheetButton("Cancel", PayraColors.Primary, onClick = { confirmLogout = false })
            }
        }
    }
}

@Composable
private fun SettingsGroup(index: Int, items: List<SettingItem>, danger: Boolean = false) {
    GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(index)) {
        Column(modifier = Modifier.padding(4.dp)) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClick(onClick = item.onClick, pressedScale = 0.97f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(item.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (danger) item.color else PayraColors.Ink,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
