package com.payra.calling.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.ErrorText
import com.payra.calling.designsystem.GlassField
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.enterFromBottom
import com.payra.calling.designsystem.popIn

/** Figma screen "19 Add Contact": phone number and email. */
@Composable
fun AddContactScreen(onBack: () -> Unit, onSaved: (String) -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "New contact", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .popIn()
                        .clip(CircleShape)
                        .background(PayraColors.Glow)
                        .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(44.dp))
                }
                GlassField(first, { first = it; error = false }, "First name", Icons.Outlined.Person, modifier = Modifier.enterFromBottom(1))
                GlassField(last, { last = it }, "Last name (optional)", Icons.Outlined.Person, modifier = Modifier.enterFromBottom(2))
                GlassField(phone, { phone = it; error = false }, "+880  Phone number", Icons.Outlined.Phone, modifier = Modifier.enterFromBottom(3), keyboardType = KeyboardType.Phone)
                GlassField(email, { email = it; error = false }, "Email address", Icons.Outlined.Mail, modifier = Modifier.enterFromBottom(4), keyboardType = KeyboardType.Email)
                InfoBanner("We will check if this person is already on the app.", modifier = Modifier.enterFromBottom(5))
                if (error) ErrorText("Add a name and a phone number or an email.")
                PayraPrimaryButton(
                    text = "Save contact",
                    modifier = Modifier.enterFromBottom(6),
                    onClick = {
                        if ((first.isBlank() && last.isBlank()) || (phone.isBlank() && email.isBlank())) {
                            error = true
                        } else {
                            val contact = FakeChatStore.addContact("$first $last".trim(), phone, email)
                            onSaved(contact.id)
                        }
                    },
                )
            }
        }
    }
}
