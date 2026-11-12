package com.payra.calling.feature.settings

import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.Audience
import com.payra.calling.data.DeviceData
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.UiPrefs
import com.payra.calling.designsystem.ErrorText
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.Wallpapers
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.feature.chats.BottomSheetOverlay
import com.payra.calling.feature.chats.SheetButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun SettingsSub(
    title: String,
    onBack: () -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                ScreenHeader(title = title, onBack = onBack)
                Spacer(Modifier.height(16.dp))
                content()
                Spacer(Modifier.height(24.dp))
            }
        }
        overlay()
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
        Text(label, fontSize = 16.sp, color = if (valueColor == PayraColors.Danger) PayraColors.Danger else PayraColors.Ink, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}

/** Figma screen "40 Notification Settings". */
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    SettingsSub("Notifications", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("MESSAGES")
                ToggleRow("Message notifications", UiPrefs.messageNotifications, { UiPrefs.messageNotifications = it })
                ToggleRow("Show message preview", UiPrefs.showPreview, { UiPrefs.showPreview = it }, sub = "Display the message text in notifications")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("CALLS")
                ToggleRow("Call notifications", UiPrefs.callNotifications, { UiPrefs.callNotifications = it })
                ToggleRow("Ringtone", UiPrefs.ringtone, { UiPrefs.ringtone = it }, sub = "Play your phone's ringtone for incoming calls")
                ToggleRow("Vibrate", UiPrefs.vibrate, { UiPrefs.vibrate = it })
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("IN-APP")
                ToggleRow("In-app sounds", UiPrefs.inAppSounds, { UiPrefs.inAppSounds = it })
            }
        }
    }
}

/** Figma screen "41 Privacy Settings": tap a row to choose who can see it. */
@Composable
fun PrivacySettingsScreen(onBack: () -> Unit, onOpenBlocked: () -> Unit = {}) {
    var showSheet by remember { mutableStateOf(false) }
    var which by remember { mutableIntStateOf(0) }
    val titles = listOf("Last seen and online", "Profile photo", "Cover photo", "About", "Who can call me")
    val current: Audience = when (which) {
        0 -> UiPrefs.lastSeen
        1 -> UiPrefs.profilePhoto
        2 -> UiPrefs.coverPhoto
        3 -> UiPrefs.about
        else -> UiPrefs.whoCanCall
    }

    fun open(index: Int) {
        which = index
        showSheet = true
    }

    fun choose(audience: Audience) {
        when (which) {
            0 -> UiPrefs.lastSeen = audience
            1 -> UiPrefs.profilePhoto = audience
            2 -> UiPrefs.coverPhoto = audience
            3 -> UiPrefs.about = audience
            else -> UiPrefs.whoCanCall = audience
        }
        showSheet = false
    }

    SettingsSub(
        title = "Privacy",
        onBack = onBack,
        overlay = {
            BottomSheetOverlay(visible = showSheet, onDismiss = { showSheet = false }) {
                Text(titles[which.coerceIn(0, titles.lastIndex)], color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Choose who can do this.", color = PayraColors.Muted, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Audience.values().forEach { audience ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick(onClick = { choose(audience) }, pressedScale = 0.97f)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(audience.label, fontSize = 16.sp, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                        if (audience == current) {
                            Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = PayraColors.Primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        },
    ) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("WHO CAN SEE MY INFO")
                ValueRow("Last seen and online", UiPrefs.lastSeen.label, onClick = { open(0) })
                ValueRow("Profile photo", UiPrefs.profilePhoto.label, onClick = { open(1) })
                ValueRow("Cover photo", UiPrefs.coverPhoto.label, onClick = { open(2) })
                ValueRow("About", UiPrefs.about.label, onClick = { open(3) })
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("MESSAGES AND CALLS")
                ToggleRow("Read receipts", UiPrefs.readReceipts, { UiPrefs.readReceipts = it }, sub = "If turned off, you will not see read receipts from others either")
                ValueRow("Who can call me", UiPrefs.whoCanCall.label, onClick = { open(4) })
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("SAFETY")
                ValueRow("Blocked contacts", "${FakeChatStore.blocked.size}", onClick = onOpenBlocked)
            }
        }
    }
}

/** Figma screen "44 Chats Settings". */
@Composable
fun ChatsSettingsScreen(onBack: () -> Unit, onOpenWallpaper: () -> Unit = {}) {
    SettingsSub("Chats", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("DISPLAY")
                ValueRow("Chat wallpaper", if (Wallpapers.customUri.isNotEmpty()) "Photo" else Wallpapers.current.name, onClick = onOpenWallpaper)
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("CHAT BEHAVIOR")
                ToggleRow("Enter key sends", UiPrefs.enterSends, { UiPrefs.enterSends = it }, sub = "Press Enter to send instead of adding a new line")
                ToggleRow("Save media to gallery", UiPrefs.saveMediaToGallery, { UiPrefs.saveMediaToGallery = it }, sub = "Photos and videos you receive are saved automatically")
            }
        }
    }
}

/** Figma screen "43 Account Settings". */
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onOpenPassword: () -> Unit = {},
    onOpenGmail: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    var showDelete by remember { mutableStateOf(false) }
    var confirmText by remember { mutableStateOf("") }

    SettingsSub(
        title = "Account",
        onBack = onBack,
        overlay = {
            BottomSheetOverlay(visible = showDelete, onDismiss = { showDelete = false }) {
                Text("Delete your account?", color = PayraColors.Danger, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "This removes your profile, chats and files from Payra for good. Type DELETE to confirm.",
                    color = PayraColors.Muted,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(12.dp))
                GlassField(value = confirmText, onValueChange = { confirmText = it }, placeholder = "Type DELETE", leadingIcon = Icons.Outlined.Lock)
                Spacer(Modifier.height(12.dp))
                SheetButton(
                    text = "Delete my account",
                    color = if (confirmText == "DELETE") PayraColors.Danger else PayraColors.Muted,
                    onClick = {
                        if (confirmText == "DELETE") {
                            showDelete = false
                            confirmText = ""
                            onDeleted()
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                SheetButton("Cancel", PayraColors.Primary, onClick = {
                    showDelete = false
                    confirmText = ""
                })
            }
        },
    ) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("SIGN-IN")
                ValueRow("Phone number", "+880 1XXX XXXXXX")
                ValueRow("Change password", "›", onClick = onOpenPassword)
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("RECOVERY AND GOOGLE")
                ValueRow("Recovery Gmail", "you@gmail.com  ✓", PayraColors.Primary, onClick = onOpenGmail)
                ValueRow("Google sign-in", "Linked ✓", PayraColors.Primary)
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2)) {
            Column {
                SectionLabel("DANGER ZONE")
                ValueRow("Delete my account", "›", PayraColors.Danger, onClick = { showDelete = true })
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }

    SettingsSub("Change password", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            GlassField(current, { current = it; error = "" }, "Current password", Icons.Outlined.Lock, isPassword = true, modifier = Modifier.enterFromBottom(0))
            GlassField(next, { next = it; error = "" }, "New password (8+ characters)", Icons.Outlined.Lock, isPassword = true, modifier = Modifier.enterFromBottom(1))
            GlassField(again, { again = it; error = "" }, "Repeat new password", Icons.Outlined.Lock, isPassword = true, modifier = Modifier.enterFromBottom(2))
            AnimatedVisibility(visible = error.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                ErrorText(error)
            }
            AnimatedVisibility(visible = done, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                InfoBanner("Password changed. (Demo only until the server is connected.)")
            }
            PayraPrimaryButton(
                text = "Change password",
                modifier = Modifier.enterFromBottom(3),
                onClick = {
                    when {
                        current.isBlank() -> error = "Enter your current password."
                        next.length < 8 -> error = "The new password needs 8 or more characters."
                        next != again -> error = "The two new passwords are different."
                        else -> {
                            error = ""
                            done = true
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun LinkGmailScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("you@gmail.com") }
    var sent by remember { mutableStateOf(false) }
    SettingsSub("Recovery Gmail", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "If you forget your password, we send a reset link to this Gmail.",
                color = PayraColors.Muted,
                fontSize = 14.sp,
                modifier = Modifier.enterFromBottom(0),
            )
            GlassField(email, { email = it; sent = false }, "Gmail address", Icons.Outlined.Mail, keyboardType = KeyboardType.Email, modifier = Modifier.enterFromBottom(1))
            PayraPrimaryButton(text = "Send verification link", modifier = Modifier.enterFromBottom(2), onClick = { if (email.contains("@")) sent = true })
            AnimatedVisibility(visible = sent, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                InfoBanner("Check your Gmail and tap the link to finish. (Demo only until the server is connected.)")
            }
        }
    }
}

/** Storage: how much the app has saved on the phone, and a real "Clear cache" button. */
@Composable
fun StorageSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cacheBytes by remember { mutableLongStateOf(0L) }
    var refresh by remember { mutableIntStateOf(0) }
    var state by remember { mutableIntStateOf(0) } // 0 idle, 1 clearing, 2 cleared
    val progress = remember { Animatable(0f) }

    LaunchedEffect(refresh) {
        cacheBytes = withContext(Dispatchers.IO) { DeviceData.cacheSizeBytes(context) }
    }

    SettingsSub("Storage and data", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("STORAGE")
                ValueRow("Cache (temporary files)", Formatter.formatShortFileSize(context, cacheBytes))
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick(
                                onClick = {
                                    if (state == 0) {
                                        state = 1
                                        scope.launch {
                                            progress.snapTo(0f)
                                            progress.animateTo(0.8f, tween(500, easing = LinearEasing))
                                            withContext(Dispatchers.IO) { DeviceData.clearCache(context) }
                                            progress.animateTo(1f, tween(200))
                                            state = 2
                                            refresh++
                                            delay(1500)
                                            state = 0
                                        }
                                    }
                                },
                                pressedScale = 0.97f,
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(PayraColors.Primary.copy(alpha = 0.12f))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = state,
                            transitionSpec = { (fadeIn(tween(160)) + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut(tween(100)) + scaleOut(targetScale = 0.8f)) },
                            label = "clearCache",
                        ) { st ->
                            when (st) {
                                0 -> Text("Clear cache", color = PayraColors.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                1 -> Text("Clearing... ${(progress.value * 100).toInt()}%", color = PayraColors.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                else -> Text("Cache cleared ✓", color = PayraColors.Success, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("DATA USE")
                ToggleRow("Download media on Wi-Fi only", UiPrefs.autoDownloadWifiOnly, { UiPrefs.autoDownloadWifiOnly = it }, sub = "Saves your mobile data")
                ToggleRow("Save media to gallery", UiPrefs.saveMediaToGallery, { UiPrefs.saveMediaToGallery = it })
            }
        }
    }
}

/** Appearance: battery-friendly "Reduce effects" and the chat wallpaper. */
@Composable
fun AppearanceSettingsScreen(onBack: () -> Unit, onOpenWallpaper: () -> Unit) {
    SettingsSub("Appearance", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("EFFECTS")
                ToggleRow(
                    label = "Reduce effects",
                    checked = UiPrefs.reduceEffects,
                    onChecked = { UiPrefs.reduceEffects = it },
                    sub = "Turns off the blur and the moving glow. Saves battery on older phones.",
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("CHATS")
                ValueRow("Chat wallpaper", if (Wallpapers.customUri.isNotEmpty()) "Photo" else Wallpapers.current.name, onClick = onOpenWallpaper)
            }
        }
    }
}

/** Figma screen "46 Help". */
@Composable
fun HelpSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    SettingsSub("Help", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("SUPPORT")
                ValueRow("FAQ", "›")
                ValueRow(
                    "Contact support", "›",
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@payra.app")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        } catch (e: Exception) {
                            // no email app
                        }
                    },
                )
                ValueRow("Report a problem", "›")
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1)) {
            Column {
                SectionLabel("LEGAL")
                ValueRow("Terms of service", "›")
                ValueRow("Privacy policy", "›")
            }
        }
    }
}

@Composable
fun AboutSettingsScreen(onBack: () -> Unit) {
    SettingsSub("About", onBack) {
        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(0)) {
            Column {
                SectionLabel("PAYRA")
                ValueRow("Version", "0.1.0")
                ValueRow("Made for", "Chat and calls")
                ValueRow("Package", "com.payra.calling")
            }
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
