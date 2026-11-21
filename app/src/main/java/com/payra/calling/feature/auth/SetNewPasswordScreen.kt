package com.payra.calling.feature.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.backend.Auth
import com.payra.calling.data.backend.AuthLink
import com.payra.calling.data.backend.AuthResult
import com.payra.calling.designsystem.ErrorText
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.enterFromBottom
import kotlinx.coroutines.launch

/** Opened from the "reset your password" link in the e-mail: choose a new password. */
@Composable
fun SetNewPasswordScreen(link: AuthLink, onDone: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(if (link.error.isNotBlank()) "This link has expired. Ask for a new reset link." else "") }
    var loading by remember { mutableStateOf(false) }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Choose a new password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PayraColors.Ink, modifier = Modifier.enterFromBottom(0))
            Text("Use 8 or more characters.", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.enterFromBottom(1))
            GlassField(password, { password = it; error = "" }, "New password", Icons.Outlined.Lock, isPassword = true, modifier = Modifier.enterFromBottom(2))
            GlassField(again, { again = it; error = "" }, "Repeat new password", Icons.Outlined.Lock, isPassword = true, modifier = Modifier.enterFromBottom(3))
            AnimatedVisibility(visible = error.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                ErrorText(error)
            }
            PayraPrimaryButton(
                text = "Save password",
                loading = loading,
                modifier = Modifier.enterFromBottom(4),
                onClick = {
                    when {
                        password.length < 8 -> error = "The password needs 8 or more characters."
                        password != again -> error = "The two passwords are different."
                        link.accessToken.isBlank() -> error = "This link has expired. Ask for a new reset link."
                        else -> {
                            loading = true
                            scope.launch {
                                val result = Auth.setPasswordWithLink(link.accessToken, password)
                                loading = false
                                if (result is AuthResult.Error) {
                                    error = result.message
                                } else {
                                    Toast.makeText(context, "Password changed. Log in with your new password.", Toast.LENGTH_LONG).show()
                                    onDone()
                                }
                            }
                        }
                    }
                },
            )
            TextLink("Cancel", onClick = onCancel, modifier = Modifier.enterFromBottom(5))
        }
    }
}
