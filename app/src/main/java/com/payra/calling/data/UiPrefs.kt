package com.payra.calling.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Audience(val label: String) {
    Everyone("Everyone"),
    Contacts("My contacts"),
    Nobody("Nobody"),
}

/** App-wide settings the person can change (kept in memory for now; saved to the account when the backend is connected). */
object UiPrefs {
    /** Turns off the blur and the moving glow to save battery. */
    var reduceEffects by mutableStateOf(false)

    var lastSeen by mutableStateOf(Audience.Everyone)
    var profilePhoto by mutableStateOf(Audience.Contacts)
    var coverPhoto by mutableStateOf(Audience.Contacts)
    var about by mutableStateOf(Audience.Everyone)
    var whoCanCall by mutableStateOf(Audience.Everyone)
    var readReceipts by mutableStateOf(true)

    var messageNotifications by mutableStateOf(true)
    var showPreview by mutableStateOf(true)
    var callNotifications by mutableStateOf(true)
    var vibrate by mutableStateOf(true)
    var inAppSounds by mutableStateOf(true)
    var ringtone by mutableStateOf(true)

    var enterSends by mutableStateOf(false)
    var saveMediaToGallery by mutableStateOf(true)
    var autoDownloadWifiOnly by mutableStateOf(true)
}
