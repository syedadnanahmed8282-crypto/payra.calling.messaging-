package com.payra.calling.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.ErrorText
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.GoogleButton
import com.payra.calling.designsystem.LogoTile
import com.payra.calling.designsystem.OrDivider
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.shake

/** Figma screen "6 Register". */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var shakeCount by remember { mutableIntStateOf(0) }

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
            Spacer(Modifier.height(8.dp))
            LogoTile(modifier = Modifier.enterFromBottom(0))
            Text(
                "Create account",
                style = MaterialTheme.typography.displayLarge,
                color = PayraColors.Ink,
                modifier = Modifier.enterFromBottom(1),
            )
            Text(
                "Sign up with your phone number and a password.",
                color = PayraColors.Muted,
                fontSize = 15.sp,
                modifier = Modifier.enterFromBottom(2),
            )

            Column(
                modifier = Modifier.enterFromBottom(3).shake(shakeCount),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GlassField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    placeholder = "Full name",
                    leadingIcon = Icons.Outlined.Person,
                )
                GlassField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    placeholder = "+880  1XXX XXXXXX",
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone,
                )
                GlassField(
                    value = password,
                    onValueChange = { password = it; showError = false },
                    placeholder = "Password (8+ characters)",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    isError = showError && password.length < 8,
                )
                AnimatedVisibility(
                    visible = showError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    ErrorText("Fill in every field. The password needs 8 or more characters.")
                }
            }

            PayraPrimaryButton(
                text = "Create account",
                modifier = Modifier.enterFromBottom(4),
                onClick = {
                    if (name.isBlank() || phone.isBlank() || password.length < 8) {
                        showError = true
                        shakeCount++
                    } else {
                        onRegistered()
                    }
                },
            )
            OrDivider(modifier = Modifier.enterFromBottom(5))
            GoogleButton(onClick = onRegistered, modifier = Modifier.enterFromBottom(6))

            Row(
                modifier = Modifier.fillMaxWidth().enterFromBottom(7),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Already have an account? ", color = PayraColors.Muted, fontSize = 14.sp)
                TextLink("Log in", onClick = onLogin)
            }
        }
    }
}
