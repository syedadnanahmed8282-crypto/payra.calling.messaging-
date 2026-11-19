package com.payra.calling.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.ErrorText
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.emailSupport
import com.payra.calling.designsystem.enterFromBottom

@Composable
private fun HelpPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
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
}

private val Faq = listOf(
    "How do I sign in?" to "Use your phone number and password, or sign in with Google. If you do not have an account yet, tap Create account on the login screen.",
    "I forgot my password." to "Tap Forgot password on the login screen. We send a reset link to the Gmail you linked to your account. If no Gmail is linked, contact support.",
    "How do I change my phone number?" to "Go to Settings > Account > Phone number, enter the new number and confirm with your password. Your chats and contacts stay the same.",
    "How do I block someone?" to "Open the person's profile and choose Block, or use the three-dot menu in the chat. Blocked people cannot message or call you. You can unblock them from the same place or from Settings > Privacy > Blocked contacts.",
    "Who can see my profile photo and last seen?" to "You decide. Go to Settings > Privacy and choose Everyone, My contacts or Nobody for each item.",
    "How long are photos and videos kept?" to "Photos and videos in chats are kept on our servers for a limited time (about 3 days) to save space. Save important ones to your phone with the download button. Profile and cover photos are kept as long as you use them.",
    "Why can I not download or open a file?" to "Check your internet connection and the free space on your phone, then try again. To open a file you need an app that can read that type of file, for example a PDF reader.",
    "How do calls work?" to "Voice and video calls need an internet connection. Tap the phone icon in a chat. During a call you can minimize it, move the small window anywhere, or drag it to the bottom to end the call.",
    "How do I delete my account?" to "Go to Settings > Account > Delete my account and type DELETE to confirm. This removes your profile, chats and files for good.",
)

/** Common questions, each answer opens with a tap. */
@Composable
fun FaqScreen(onBack: () -> Unit) {
    var open by remember { mutableIntStateOf(-1) }
    HelpPage("FAQ", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Faq.forEachIndexed { index, (question, answer) ->
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(index), shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier
                            .bouncyClick(onClick = { open = if (open == index) -1 else index }, pressedScale = 0.98f)
                            .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(question, color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(if (open == index) "–" else "+", color = PayraColors.Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        AnimatedVisibility(
                            visible = open == index,
                            enter = fadeIn(tween(200)) + expandVertically(),
                            exit = fadeOut(tween(120)) + shrinkVertically(),
                        ) {
                            Text(answer, color = PayraColors.Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
    }
}

private val ReportKinds = listOf("App bug", "Messages", "Calls", "Account", "Other")

/** Tell us what went wrong. Opens your email app with the report (and a screenshot) ready to send. */
@Composable
fun ReportProblemScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var kind by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var screenshot by remember { mutableStateOf<android.net.Uri?>(null) }
    var error by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) screenshot = uri
    }

    HelpPage("Report a problem", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("What kind of problem is it?", color = PayraColors.Muted, fontSize = 14.sp, modifier = Modifier.enterFromBottom(0))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportKinds.forEachIndexed { index, label ->
                    val bg by animateColorAsState(
                        targetValue = if (kind == index) PayraColors.Primary else PayraColors.Primary.copy(alpha = 0.10f),
                        animationSpec = tween(160),
                        label = "kindChip",
                    )
                    Text(
                        text = label,
                        color = if (kind == index) Color.White else PayraColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(bg)
                            .bouncyClick(onClick = { kind = index }, pressedScale = 0.93f)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    )
                }
            }
            GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1), shape = RoundedCornerShape(24.dp)) {
                Box(modifier = Modifier.padding(16.dp).heightIn(min = 140.dp)) {
                    if (text.isEmpty()) {
                        Text("Describe what happened and what you expected...", color = PayraColors.Muted, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it; error = ""; sent = false },
                        textStyle = TextStyle(color = PayraColors.Ink, fontSize = 15.sp),
                        cursorBrush = SolidColor(PayraColors.Primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            GlassSurface(
                modifier = Modifier.fillMaxWidth().enterFromBottom(2),
                shape = RoundedCornerShape(24.dp),
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    val shot = screenshot
                    if (shot != null) {
                        PayraImage(model = shot, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
                    } else {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (shot != null) "Screenshot added" else "Add a screenshot (optional)",
                        color = PayraColors.Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (shot != null) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Remove screenshot",
                            tint = PayraColors.Muted,
                            modifier = Modifier.size(22.dp).bouncyClick(onClick = { screenshot = null }, pressedScale = 0.8f),
                        )
                    }
                }
            }
            AnimatedVisibility(visible = error.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                ErrorText(error)
            }
            AnimatedVisibility(visible = sent, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                InfoBanner("Thank you! Your email app opened with the report. Press send there.")
            }
            PayraPrimaryButton(
                text = "Send report",
                modifier = Modifier.enterFromBottom(3),
                onClick = {
                    if (text.isBlank()) {
                        error = "Please describe the problem."
                    } else {
                        emailSupport(
                            context = context,
                            subject = "Problem report: ${ReportKinds[kind]}",
                            body = text.trim() + "\n\n---\nApp: Payra 0.1.0\nPhone: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})",
                            attachment = screenshot,
                        )
                        sent = true
                    }
                },
            )
        }
    }
}

enum class LegalKind { Terms, Privacy }

private class LegalSection(val heading: String, val body: String)

private val TermsText = listOf(
    LegalSection("1. Using Payra", "By creating an account or using Payra you agree to these terms. Please read them carefully. If you do not agree, please do not use the app."),
    LegalSection("2. Your account", "You sign in with your phone number and a password, or with Google. Keep your password private. You are responsible for what happens under your account. Give correct information and keep it up to date."),
    LegalSection("3. Acceptable use", "Do not use Payra to harass, threaten or scam people, to send spam, to share illegal content, or to share content that harms children. Do not try to break, copy or misuse the service or other people's accounts."),
    LegalSection("4. Your content", "Messages, photos, videos, voice notes and files you send stay yours. You allow Payra to store and deliver them so the service can work. Photos and videos in chats are kept for a limited time to save space."),
    LegalSection("5. Calls and messages", "Voice and video calls and messages need an internet connection. Call and message quality depends on your connection and your phone."),
    LegalSection("6. Blocking and reporting", "You can block and report people at any time. We may limit or close accounts that break these terms."),
    LegalSection("7. Ending your account", "You can delete your account any time from Settings > Account. This removes your profile, chats and files."),
    LegalSection("8. Changes", "We may update these terms. If the change is important, we will tell you in the app. Using Payra after a change means you accept it."),
    LegalSection("9. Contact", "Questions about these terms? Write to $SupportMail."),
)

private val PrivacyText = listOf(
    LegalSection("What we collect", "Your phone number, name, profile and cover photo, the messages, photos, videos, voice notes and files you send, call details (who, when, how long), and your phone model for fixing problems. Contacts are used only if you allow it, to help you find people."),
    LegalSection("How we use it", "To create your account, deliver your messages and calls, show your profile to people you allow, send notifications, keep the service safe, and fix problems."),
    LegalSection("Where it is kept", "Messages and account data are kept with our service providers for database, media storage, calling and notifications. Photos and videos in chats are removed from the server after a limited time."),
    LegalSection("Who can see it", "Only the people you chat with can see your messages. You choose who sees your last seen, profile photo, cover photo and about in Settings > Privacy. We do not sell your personal information."),
    LegalSection("Your choices", "You can change your privacy settings, block people, turn notifications off, and delete your account. When you delete your account, your profile, chats and files are removed."),
    LegalSection("Security", "We use secure connections and access rules so people can only reach their own data. No service is perfect, so please keep your password private."),
    LegalSection("Children", "Payra is not meant for children under 13."),
    LegalSection("Contact", "Questions about privacy? Write to $SupportMail."),
)

private const val SupportMail = "support@payra.app"

/** Terms of service / Privacy policy page. */
@Composable
fun LegalScreen(kind: LegalKind, onBack: () -> Unit) {
    val sections = if (kind == LegalKind.Terms) TermsText else PrivacyText
    HelpPage(if (kind == LegalKind.Terms) "Terms of service" else "Privacy policy", onBack) {
        Text("Last updated: November 2026", color = PayraColors.Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sections.forEachIndexed { index, section ->
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(index), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(section.heading, color = PayraColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(section.body, color = PayraColors.Muted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
