package com.payra.calling.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

/** Figma screen "26 Forgot Password". The reset link goes to the Gmail linked to the account. */
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Forgot password", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(36.dp))
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GlassSurface(
                    modifier = Modifier.size(72.dp).popIn(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = PayraColors.Primary,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                    )
                }
                Text(
                    "Reset your password",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PayraColors.Ink,
                    modifier = Modifier.enterFromBottom(1),
                )
                Text(
                    "Enter your phone number. We will send a reset link to the Gmail linked to your account.",
                    color = PayraColors.Muted,
                    fontSize = 15.sp,
                    modifier = Modifier.enterFromBottom(2),
                )
                GlassField(
                    value = phone,
                    onValueChange = { phone = it; sent = false },
                    placeholder = "+880  1XXX XXXXXX",
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.enterFromBottom(3),
                )
                PayraPrimaryButton(
                    text = "Send reset link",
                    onClick = { if (phone.isNotBlank()) sent = true },
                    modifier = Modifier.enterFromBottom(4),
                )
                AnimatedVisibility(
                    visible = sent,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    InfoBanner("If a Gmail is linked to this number, a reset link is on its way.")
                }
                TextLink("No Gmail linked? Contact support", onClick = {}, modifier = Modifier.enterFromBottom(5))
            }
        }
    }
}
