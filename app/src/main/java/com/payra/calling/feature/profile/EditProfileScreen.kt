package com.payra.calling.feature.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/** Figma screen "23 Edit Profile": really choose a cover photo and a profile photo from your gallery. */
@Composable
fun EditProfileScreen(contactId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val contact = FakeChatStore.contact(contactId)
    var name by remember { mutableStateOf(contact.name) }
    var about by remember { mutableStateOf(contact.about) }

    fun keepAccess(uri: android.net.Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            // not every picker allows this; the picture still works for this session
        }
    }

    val pickAvatar = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            keepAccess(uri)
            FakeChatStore.updateContact(contactId) { it.copy(avatarUri = uri.toString()) }
        }
    }
    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            keepAccess(uri)
            FakeChatStore.updateContact(contactId) { it.copy(coverUri = uri.toString()) }
        }
    }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

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
                    .height(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(contact.color.copy(alpha = 0.5f))
                    .bouncyClick(onClick = { pickCover.launch(imageOnly) }, pressedScale = 0.98f),
            ) {
                if (contact.coverUri.isNotEmpty()) {
                    PayraImage(model = contact.coverUri, modifier = Modifier.fillMaxSize())
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.40f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Change cover", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cover photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(modifier = Modifier.height(52.dp).padding(start = 24.dp).enterFromBottom(2)) {
                Box(modifier = Modifier.offset(y = (-48).dp).size(100.dp)) {
                    AvatarCircle(
                        initial = contact.name.take(1),
                        color = contact.color,
                        size = 100.dp,
                        imageUri = contact.avatarUri,
                        modifier = Modifier
                            .border(4.dp, PayraColors.Background, CircleShape)
                            .bouncyClick(onClick = { pickAvatar.launch(imageOnly) }, pressedScale = 0.95f),
                    )
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

            Row(modifier = Modifier.padding(start = 140.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (contact.avatarUri.isNotEmpty()) {
                    TextLink("Remove photo", onClick = { FakeChatStore.updateContact(contactId) { it.copy(avatarUri = "") } }, color = PayraColors.Danger)
                }
                if (contact.coverUri.isNotEmpty()) {
                    TextLink("Remove cover", onClick = { FakeChatStore.updateContact(contactId) { it.copy(coverUri = "") } }, color = PayraColors.Danger)
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassField(value = name, onValueChange = { name = it }, placeholder = "Your name", leadingIcon = Icons.Outlined.Person, modifier = Modifier.enterFromBottom(3))
                GlassField(value = about, onValueChange = { about = it }, placeholder = "About", leadingIcon = Icons.Outlined.Info, modifier = Modifier.enterFromBottom(4))
                GlassField(value = "+880 1XXX XXXXXX", onValueChange = {}, placeholder = "Phone", leadingIcon = Icons.Outlined.Lock, modifier = Modifier.enterFromBottom(5))
                GlassField(value = "you@gmail.com", onValueChange = {}, placeholder = "Gmail", leadingIcon = Icons.Outlined.Mail, modifier = Modifier.enterFromBottom(6))
            }
            Spacer(Modifier.height(20.dp))
            PayraPrimaryButton(
                text = "Save changes",
                modifier = Modifier.enterFromBottom(7),
                onClick = {
                    FakeChatStore.updateContact(contactId) { it.copy(name = name.ifBlank { it.name }, about = about) }
                    onBack()
                },
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
