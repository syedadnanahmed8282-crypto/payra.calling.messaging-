package com.payra.calling.feature.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

/**
 * Figma screens "21 Contact Profile" and "22 My Profile".
 * The profile color (stands in for the cover image's main color) tints the whole screen, Spotify-style.
 */
@Composable
fun ProfileScreen(
    contactId: String,
    isSelf: Boolean,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onCall: (String, Boolean) -> Unit,
    onEditProfile: () -> Unit,
) {
    val contact = remember(contactId) { FakeChatStore.contact(contactId) }
    val accent by animateColorAsState(targetValue = contact.color, animationSpec = tween(600), label = "profileAccent")
    val noAction: () -> Unit = {}

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
            // Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.60f), accent.copy(alpha = 0.10f)))),
            )
            // Avatar: half over the cover, half below
            Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.TopCenter) {
                AvatarCircle(
                    initial = contact.name.take(1),
                    color = accent,
                    size = 104.dp,
                    modifier = Modifier.offset(y = (-52).dp).popIn().border(4.dp, PayraColors.Background, CircleShape),
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
                    text = if (isSelf) contact.about else "online",
                    fontSize = 14.sp,
                    color = if (isSelf) PayraColors.Muted else accent,
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
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Media and files", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                                Text("See all", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = accent)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0.7f, 1f, 0.5f, 0.85f).forEach { a ->
                                    Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = a)))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth().enterFromBottom(5),
                        shape = RoundedCornerShape(24.dp),
                        onClick = noAction,
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Block, contentDescription = null, tint = PayraColors.Danger, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Block ${contact.name}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Danger)
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        // Back / edit buttons float over the cover
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
            Spacer(Modifier.weight(1f))
            GlassCircleButton(
                icon = if (isSelf) Icons.Outlined.Edit else Icons.Outlined.MoreVert,
                contentDescription = if (isSelf) "Edit profile" else "More",
                onClick = if (isSelf) onEditProfile else noAction,
            )
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
