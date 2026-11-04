package com.payra.calling.feature.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.IllustrationCircle
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

data class PermissionReason(val icon: ImageVector, val title: String, val description: String)

data class PermissionPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val primaryLabel: String,
    val skipLabel: String,
    val reasons: List<PermissionReason> = emptyList(),
    /** Android permissions requested when the main button is pressed. */
    val permissions: List<String> = emptyList(),
)

object PermissionPages {
    val Contacts = PermissionPage(
        icon = Icons.Outlined.People,
        title = "Find your friends",
        subtitle = "Allow contacts so we can show who is already on the app. Your contacts are never shown to others.",
        primaryLabel = "Allow contacts",
        skipLabel = "Not now",
        permissions = listOf(Manifest.permission.READ_CONTACTS),
    )
    val MicAndCamera = PermissionPage(
        icon = Icons.Outlined.CameraAlt,
        title = "Allow mic and camera",
        subtitle = "You need these to make voice and video calls and to send photos and voice messages.",
        primaryLabel = "Continue",
        skipLabel = "Not now",
        reasons = listOf(
            PermissionReason(Icons.Outlined.Mic, "Microphone", "For voice calls and voice messages"),
            PermissionReason(Icons.Outlined.CameraAlt, "Camera", "For video calls and taking photos"),
        ),
        permissions = listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
    )
    val Notifications = PermissionPage(
        icon = Icons.Outlined.Notifications,
        title = "Never miss a message",
        subtitle = "Turn on notifications to get new messages and incoming calls right away.",
        primaryLabel = "Turn on notifications",
        skipLabel = "Maybe later",
        permissions = if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList(),
    )
}

/** Figma screens "50/51/52 Permission". The main button opens the real Android permission dialog. */
@Composable
fun PermissionScreen(page: PermissionPage, onAllow: () -> Unit, onSkip: () -> Unit) {
    // Shows the real Android permission dialog; whatever the user answers, we move on to the next screen.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        onAllow()
    }

    GlowBackground {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            IllustrationCircle(icon = page.icon, modifier = Modifier.popIn())
            Spacer(Modifier.height(20.dp))
            Text(
                page.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PayraColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterFromBottom(1),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                page.subtitle,
                fontSize = 15.sp,
                color = PayraColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterFromBottom(2),
            )

            if (page.reasons.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(3)) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        page.reasons.forEach { reason ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ReasonIcon(reason.icon)
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(reason.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                    Text(reason.description, fontSize = 13.sp, color = PayraColors.Muted)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            PayraPrimaryButton(
                text = page.primaryLabel,
                modifier = Modifier.enterFromBottom(4),
                onClick = {
                    if (page.permissions.isEmpty()) onAllow() else launcher.launch(page.permissions.toTypedArray())
                },
            )
            Spacer(Modifier.height(14.dp))
            TextLink(page.skipLabel, onClick = onSkip, modifier = Modifier.enterFromBottom(5))
        }
    }
}

@Composable
private fun ReasonIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PayraColors.Primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
    }
}
