package com.payra.calling.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "23 Edit Profile": cover photo, profile photo, name, about. */
@Composable
fun EditProfileScreen(contactId: String, onBack: () -> Unit) {
    val contact = remember(contactId) { FakeChatStore.contact(contactId) }
    var name by remember { mutableStateOf(contact.name) }
    var about by remember { mutableStateOf(contact.about) }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Edit profile", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .enterFromBottom(1)
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(contact.color.copy(alpha = 0.5f)),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .bouncyClick(onClick = {}, pressedScale = 0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Change cover", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Box(modifier = Modifier.height(48.dp).padding(start = 24.dp).enterFromBottom(2)) {
                Box(modifier = Modifier.offset(y = (-48).dp).size(96.dp)) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(contact.color)
                            .border(4.dp, PayraColors.Background, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(contact.name.take(1), color = Color.White, fontSize = 38.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PayraColors.Primary)
                            .border(3.dp, PayraColors.Background, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassField(value = name, onValueChange = { name = it }, placeholder = "Your name", leadingIcon = Icons.Outlined.Person, modifier = Modifier.enterFromBottom(3))
                GlassField(value = about, onValueChange = { about = it }, placeholder = "About", leadingIcon = Icons.Outlined.Info, modifier = Modifier.enterFromBottom(4))
                GlassField(value = "+880 1XXX XXXXXX", onValueChange = {}, placeholder = "Phone", leadingIcon = Icons.Outlined.Lock, modifier = Modifier.enterFromBottom(5))
                GlassField(value = "you@gmail.com", onValueChange = {}, placeholder = "Gmail", leadingIcon = Icons.Outlined.Mail, modifier = Modifier.enterFromBottom(6))
            }
            Spacer(Modifier.height(20.dp))
            PayraPrimaryButton(text = "Save changes", onClick = onBack, modifier = Modifier.enterFromBottom(7))
            Spacer(Modifier.height(20.dp))
        }
    }
}
