package com.payra.calling.feature.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.DeviceContact
import com.payra.calling.data.DeviceData
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSearchField
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AvatarColors = listOf(
    Color(0xFF2B8FAE), Color(0xFFF28B66), Color(0xFF7B6FD6), Color(0xFF3FA796), Color(0xFFE4A93A),
)

/** Pick a person from the phone's contacts (or the app's own contacts) to send as a contact card. */
@Composable
fun ContactPickerScreen(onBack: () -> Unit, onPicked: (name: String, phone: String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var contacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(hasPermission) {
        contacts = if (hasPermission) {
            withContext(Dispatchers.IO) { DeviceData.loadContacts(context) }
        } else {
            emptyList()
        }
    }

    // If the phone has no contacts (or no permission), fall back to the people already in the app.
    val source = if (contacts.isNotEmpty()) {
        contacts
    } else {
        FakeChatStore.contacts.map { DeviceContact(it.name, "+880 1XXX XXXXXX") }
    }
    val filtered = if (query.isBlank()) source else source.filter {
        it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
    }

    GlassScaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ScreenHeader(title = "Send a contact", onBack = onBack)
                Spacer(Modifier.height(10.dp))
                GlassSearchField(value = query, onValueChange = { query = it }, placeholder = "Search contacts")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
        ) {
            if (!hasPermission) {
                item {
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        InfoBanner("Allow contacts to choose from your phone's address book.")
                        Spacer(Modifier.height(10.dp))
                        PayraPrimaryButton(text = "Allow contacts", onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) })
                    }
                }
            }
            item {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (filtered.isEmpty()) {
                            Text("No contacts found", color = PayraColors.Muted, fontSize = 15.sp, modifier = Modifier.padding(16.dp))
                        }
                        filtered.take(300).forEachIndexed { index, contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick(onClick = { onPicked(contact.name, contact.phone) }, pressedScale = 0.97f)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AvatarCircle(
                                    initial = contact.name.take(1).ifBlank { "?" },
                                    color = AvatarColors[index % AvatarColors.size],
                                    size = 46.dp,
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                                    Text(contact.phone, fontSize = 13.sp, color = PayraColors.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
