package com.payra.calling.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MessageKind
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn
import com.payra.calling.designsystem.rememberDominantColor
import com.payra.calling.designsystem.shareText
import com.payra.calling.feature.chats.BottomSheetOverlay
import com.payra.calling.feature.chats.MediaTile
import com.payra.calling.feature.chats.MenuRow
import com.payra.calling.feature.chats.SheetButton

/**
 * Figma screens "21 Contact Profile" and "22 My Profile".
 * The main color of the cover photo (or profile photo) tints the whole screen. Tap the cover or the photo to see
 * it full screen. The three-dot menu shares the contact, shows media, blocks, or reports.
 */
@Composable
fun ProfileScreen(
    contactId: String,
    isSelf: Boolean,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onCall: (String, Boolean) -> Unit,
    onEditProfile: () -> Unit,
    onOpenSharedMedia: () -> Unit,
    onViewImage: (MediaItem, String) -> Unit,
) {
    val context = LocalContext.current
    val contact = FakeChatStore.contact(contactId)
    val convo = remember(contactId) { FakeChatStore.conversationFor(contactId) }
    val isBlocked = contactId in FakeChatStore.blocked

    val baseColor = rememberDominantColor(contact.coverUri.ifEmpty { contact.avatarUri }, contact.color)
    val accent by animateColorAsState(targetValue = baseColor, animationSpec = tween(600), label = "profileAccent")

    var showMenu by remember { mutableStateOf(false) }
    var showBlock by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    val mediaItems = convo.messages
        .filter { !it.deleted && (it.kind == MessageKind.Image || it.kind == MessageKind.Video) }
        .flatMap { it.media }
        .take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(PayraColors.Background)
                    val topCenter = Offset(size.width / 2f, 0f)
                    drawCircle(
                        brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.40f), Color.Transparent), center = topCenter, radius = size.width * 1.0f),
                        radius = size.width * 1.0f,
                        center = topCenter,
                    )
                    val mid = Offset(size.width / 2f, size.height * 0.55f)
                    drawCircle(
                        brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.16f), Color.Transparent), center = mid, radius = size.width * 1.1f),
                        radius = size.width * 1.1f,
                        center = mid,
                    )
                },
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Cover (tap to see it full screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.60f), accent.copy(alpha = 0.10f))))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            if (contact.coverUri.isNotEmpty()) onViewImage(MediaItem(0, false, "", contact.coverUri), "Cover photo")
                        },
                ) {
                    if (contact.coverUri.isNotEmpty()) {
                        PayraImage(model = contact.coverUri, modifier = Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, PayraColors.Background.copy(alpha = 0.55f)))),
                        )
                    }
                }
                // Avatar: half over the cover, half below (tap to see it full screen)
                Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.TopCenter) {
                    AvatarCircle(
                        initial = contact.name.take(1),
                        color = accent,
                        size = 104.dp,
                        imageUri = contact.avatarUri,
                        modifier = Modifier
                            .offset(y = (-52).dp)
                            .popIn()
                            .border(4.dp, PayraColors.Background, CircleShape)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                if (contact.avatarUri.isNotEmpty()) onViewImage(MediaItem(0, false, "", contact.avatarUri), "Profile photo")
                            },
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(10.dp))
                    Text(contact.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(0))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isSelf) contact.about else if (isBlocked) "Blocked" else "online",
                        fontSize = 14.sp,
                        color = if (isSelf) PayraColors.Muted else if (isBlocked) PayraColors.Danger else accent,
                        fontWeight = if (isSelf) FontWeight.Normal else FontWeight.SemiBold,
                        modifier = Modifier.enterFromBottom(1),
                    )
                    Spacer(Modifier.height(20.dp))

                    if (isSelf) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .enterFromBottom(2)
                                .bouncyClick(onClick = onEditProfile, pressedScale = 0.96f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(accent)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Edit profile", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    } else {
                        Row(modifier = Modifier.enterFromBottom(2), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                            ActionCircle(Icons.Outlined.ChatBubbleOutline, "Message", accent) { onOpenChat(contactId) }
                            ActionCircle(Icons.Outlined.Call, "Audio", accent) { onCall(contactId, false) }
                            ActionCircle(Icons.Outlined.Videocam, "Video", accent) { onCall(contactId, true) }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    val rows: List<Triple<ImageVector, String, String>> = if (isSelf) {
                        listOf(
                            Triple(Icons.Outlined.Phone, "Phone", "+880 1XXX XXXXXX"),
                            Triple(Icons.Outlined.Mail, "Email (Gmail)", "you@gmail.com"),
                            Triple(Icons.Outlined.Info, "About", contact.about),
                        )
                    } else {
                        listOf(
                            Triple(Icons.Outlined.Phone, "Phone", "+880 1XXX XXXXXX"),
                            Triple(Icons.Outlined.Mail, "Email", "user@gmail.com"),
                            Triple(Icons.Outlined.Info, "About", contact.about),
                        )
                    }

                    GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(3)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            rows.forEach { (icon, label, value) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(label, fontSize = 12.sp, color = PayraColors.Muted)
                                        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                    }
                                }
                            }
                        }
                    }

                    if (!isSelf) {
                        Spacer(Modifier.height(14.dp))
                        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(4), shape = RoundedCornerShape(24.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().bouncyClick(onClick = onOpenSharedMedia, pressedScale = 0.98f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Media, files and links", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                                    Text("See all", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = accent)
                                }
                                Spacer(Modifier.height(10.dp))
                                if (mediaItems.isEmpty()) {
                                    Text("Nothing shared yet", fontSize = 13.sp, color = PayraColors.Muted)
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        mediaItems.forEach { item ->
                                            MediaTile(
                                                item = item,
                                                modifier = Modifier.size(64.dp),
                                                onClick = { onViewImage(item, "") },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(5), shape = RoundedCornerShape(24.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (convo.muted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Mute notifications", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = convo.muted,
                                    onCheckedChange = { convo.muted = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = PayraColors.Ink.copy(alpha = 0.18f),
                                        uncheckedBorderColor = Color.Transparent,
                                    ),
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth().enterFromBottom(6),
                            shape = RoundedCornerShape(24.dp),
                            onClick = {
                                if (isBlocked) {
                                    FakeChatStore.unblock(contactId)
                                } else {
                                    showBlock = true
                                }
                            },
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Block, contentDescription = null, tint = if (isBlocked) accent else PayraColors.Danger, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (isBlocked) "Unblock ${contact.name}" else "Block ${contact.name}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBlocked) accent else PayraColors.Danger,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }

            // Back / edit / menu buttons float over the cover
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
                Spacer(Modifier.weight(1f))
                GlassCircleButton(
                    icon = if (isSelf) Icons.Outlined.Edit else Icons.Outlined.MoreVert,
                    contentDescription = if (isSelf) "Edit profile" else "More",
                    onClick = {
                        if (isSelf) {
                            onEditProfile()
                        } else {
                            showMenu = true
                        }
                    },
                )
            }
        }

        // Three-dot menu (above everything)
        AnimatedVisibility(visible = showMenu, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showMenu = false },
            )
        }
        AnimatedVisibility(
            visible = showMenu,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 60.dp, end = 16.dp),
            enter = scaleIn(spring(dampingRatio = 0.65f, stiffness = 450f), initialScale = 0.7f, transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(tween(140)),
            exit = scaleOut(targetScale = 0.8f, transformOrigin = TransformOrigin(1f, 0f)) + fadeOut(tween(100)),
        ) {
            GlassSurface(modifier = Modifier.width(240.dp), shape = RoundedCornerShape(24.dp), fillAlpha = 0.94f) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    MenuRow(Icons.Outlined.Share, "Share contact") {
                        showMenu = false
                        shareText(context, "${contact.name} on Payra", "Share contact")
                    }
                    MenuRow(Icons.Outlined.PhotoLibrary, "Media, files and links") {
                        showMenu = false
                        onOpenSharedMedia()
                    }
                    MenuRow(Icons.Outlined.Flag, "Report") {
                        showMenu = false
                        showReport = true
                    }
                    MenuRow(
                        icon = Icons.Outlined.Block,
                        label = if (isBlocked) "Unblock" else "Block",
                        tint = if (isBlocked) PayraColors.Ink else PayraColors.Danger,
                    ) {
                        showMenu = false
                        if (isBlocked) {
                            FakeChatStore.unblock(contactId)
                        } else {
                            showBlock = true
                        }
                    }
                }
            }
        }

        BottomSheetOverlay(visible = showBlock, onDismiss = { showBlock = false }) {
            Text("Block ${contact.name}?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("They will not be able to message or call you.", color = PayraColors.Muted, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            SheetButton("Block", PayraColors.Danger, onClick = {
                FakeChatStore.block(contactId)
                showBlock = false
            })
            Spacer(Modifier.height(10.dp))
            SheetButton("Cancel", PayraColors.Primary, onClick = { showBlock = false })
        }

        BottomSheetOverlay(visible = showReport, onDismiss = { showReport = false }) {
            Text(if (reported) "Thank you" else "Report ${contact.name}?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                if (reported) "We received your report and will take a look." else "Tell us if this person is sending spam or unwanted messages.",
                color = PayraColors.Muted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(14.dp))
            if (!reported) {
                SheetButton("Report", PayraColors.Danger, onClick = { reported = true })
                Spacer(Modifier.height(10.dp))
            }
            SheetButton(if (reported) "Close" else "Cancel", PayraColors.Primary, onClick = {
                showReport = false
                reported = false
            })
        }
    }
}

@Composable
private fun ActionCircle(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GlassSurface(modifier = Modifier.size(56.dp), shape = CircleShape, onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.align(Alignment.Center).size(24.dp))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
    }
}
