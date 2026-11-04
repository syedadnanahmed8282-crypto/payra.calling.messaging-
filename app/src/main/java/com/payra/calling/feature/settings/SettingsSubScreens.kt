package com.payra.calling.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

@Composable
private fun SettingsSub(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(title = title, onBack = onBack)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = PayraColors.Muted,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit, sub: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (sub.isEmpty()) 12.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
            if (sub.isNotEmpty()) Text(sub, fontSize = 13.sp, color = PayraColors.Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PayraColors.Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = PayraColors.Ink.copy(alpha = 0.18f),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun ValueRow(label: String, value: String, valueColor: Color = PayraColors.Muted, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().bouncyClick(onClick = onClick, pressedScale = 0.98f).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 16.sp, color = PayraColors.Ink, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}

/** Figma screen "40 Notification Settings". */
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    var msg by remember { mutableStateOf(true) }
    var preview by remember { mutableStateOf(true) }
    var call by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }
    var inApp by remember { mutableStateOf(false) }
    SettingsSub("Notifications", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("MESSAGES")
                ToggleRow("Message notifications", msg, { msg = it })
                ToggleRow("Show message preview", preview, { preview = it }, sub = "Display the message text in notifications")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("CALLS")
                ToggleRow("Call notifications", call, { call = it })
                ToggleRow("Vibrate", vibrate, { vibrate = it })
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("IN-APP")
                ToggleRow("In-app sounds", inApp, { inApp = it })
            }
        }
    }
}

/** Figma screen "41 Privacy Settings". */
@Composable
fun PrivacySettingsScreen(onBack: () -> Unit, onOpenBlocked: () -> Unit = {}) {
    var receipts by remember { mutableStateOf(true) }
    SettingsSub("Privacy", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("WHO CAN SEE MY INFO")
                ValueRow("Last seen and online", "Everyone")
                ValueRow("Profile photo", "My contacts")
                ValueRow("Cover photo", "My contacts")
                ValueRow("About", "Everyone")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("MESSAGES AND CALLS")
                ToggleRow("Read receipts", receipts, { receipts = it }, sub = "If turned off, you will not see read receipts from others either")
                ValueRow("Who can call me", "Everyone")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("SAFETY")
                ValueRow("Blocked contacts", "${com.payra.calling.data.FakeChatStore.blocked.size}", onClick = onOpenBlocked)
            }
        }
    }
}

/** Figma screen "44 Chats Settings". */
@Composable
fun ChatsSettingsScreen(onBack: () -> Unit, onOpenWallpaper: () -> Unit = {}) {
    var autoSave by remember { mutableStateOf(true) }
    var enterSends by remember { mutableStateOf(false) }
    SettingsSub("Chats", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("DISPLAY")
                ValueRow("Font size", "Medium")
                ValueRow("Chat wallpaper", com.payra.calling.designsystem.Wallpapers.current.name, onClick = onOpenWallpaper)
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("CHAT BEHAVIOR")
                ToggleRow("Enter key sends", enterSends, { enterSends = it }, sub = "Press Enter to send instead of adding a new line")
                ValueRow("Media auto-download", "Wi-Fi only")
                ToggleRow("Save media to gallery", autoSave, { autoSave = it }, sub = "Photos and videos you receive are saved automatically")
            }
        }
    }
}

/** Figma screen "43 Account Settings". */
@Composable
fun AccountSettingsScreen(onBack: () -> Unit) {
    SettingsSub("Account", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("SIGN-IN")
                ValueRow("Phone number", "+880 1XXX XXXXXX")
                ValueRow("Change password", "")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("RECOVERY AND GOOGLE")
                ValueRow("Recovery Gmail", "you@gmail.com  ✓ Linked", PayraColors.Primary)
                ValueRow("Google sign-in", "Linked ✓", PayraColors.Primary)
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("DANGER ZONE")
                ValueRow("Delete my account", "", PayraColors.Danger)
            }
        }
    }
}

/** Figma screen "46 Help". */
@Composable
fun HelpSettingsScreen(onBack: () -> Unit) {
    SettingsSub("Help", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("SUPPORT")
                ValueRow("FAQ", "")
                ValueRow("Contact support", "")
                ValueRow("Report a problem", "")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("LEGAL")
                ValueRow("Terms of service", "")
                ValueRow("Privacy policy", "")
            }
        }
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Version 0.1.0", color = PayraColors.Muted, fontSize = 13.sp)
        }
    }
}

/** Figma screen "45 Language". */
@Composable
fun LanguageSettingsScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf("English") }
    SettingsSub("Language", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("APP LANGUAGE")
                listOf("System default", "English", "Bangla").forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick(onClick = { selected = language }, pressedScale = 0.97f)
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(language, fontSize = 16.sp, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                        if (selected == language) {
                            Text("✓", fontSize = 18.sp, color = PayraColors.Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
