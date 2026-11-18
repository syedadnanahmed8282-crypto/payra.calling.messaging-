package com.payra.calling.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class Contact(
    val id: String,
    val name: String,
    val about: String,
    val color: Color,
    /** Real profile photo / cover picture (a content URI as text), empty = colored letter. */
    val avatarUri: String = "",
    val coverUri: String = "",
)

enum class MessageKind { Text, Image, Video, Voice, File, Location, ContactCard, CallLog }

enum class MessageStatus { Sending, Sent, Delivered, Read, Failed }

/** A fake photo / video (just a colored picture) until real media is connected. [tone] picks the colors. */
data class MediaItem(val tone: Int, val isVideo: Boolean = false, val duration: String = "", val uri: String = "")

data class Reaction(val emoji: String, val count: Int, val mine: Boolean)

data class ReplyRef(val messageId: Long, val authorName: String, val preview: String)

data class ChatMessage(
    val id: Long,
    val sent: Boolean,
    val time: String,
    val kind: MessageKind = MessageKind.Text,
    /** Message text, or the caption of photos / videos. */
    val text: String = "",
    val media: List<MediaItem> = emptyList(),
    val voiceDuration: String = "",
    /** Path of a really recorded voice message (empty for the fake ones). */
    val audioPath: String = "",
    val fileName: String = "",
    val fileInfo: String = "",
    val fileUri: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    /** Call log entries (kind = CallLog): video or voice, and whether it was missed. */
    val callVideo: Boolean = false,
    val callMissed: Boolean = false,
    val replyTo: ReplyRef? = null,
    val edited: Boolean = false,
    val forwarded: Boolean = false,
    val deleted: Boolean = false,
    val status: MessageStatus = MessageStatus.Read,
    val reactions: List<Reaction> = emptyList(),
)

/** Short text for the chat list and for reply quotes. */
fun ChatMessage.preview(): String {
    if (deleted) return "This message was deleted"
    return when (kind) {
        MessageKind.Text -> text
        MessageKind.Image -> if (media.size > 1) "${media.size} photos" else "Photo"
        MessageKind.Video -> "Video"
        MessageKind.Voice -> "Voice message"
        MessageKind.File -> fileName.ifBlank { "Document" }
        MessageKind.Location -> "Location"
        MessageKind.ContactCard -> "Contact: $contactName"
        MessageKind.CallLog -> if (callMissed) "Missed ${if (callVideo) "video" else "voice"} call" else if (callVideo) "Video call" else "Voice call"
    }
}

class Conversation(val contact: Contact) {
    val messages = mutableStateListOf<ChatMessage>()
    var unread by mutableIntStateOf(0)
    var pinned by mutableStateOf(false)
    var muted by mutableStateOf(false)
    var archived by mutableStateOf(false)

    /** -1 = use the app wallpaper; otherwise the index of a wallpaper used only in this chat. */
    var wallpaper by mutableIntStateOf(-1)
    var wallpaperUri by mutableStateOf("")
}

private val LinkTlds = setOf(
    "com", "net", "org", "io", "app", "me", "co", "dev", "info", "biz", "xyz", "online", "site", "store", "shop",
    "tv", "ly", "gl", "gd", "be", "bd", "in", "pk", "uk", "us", "ca", "au", "de", "fr", "edu", "gov",
)

private val LinkRegex = Regex(
    "(?i)(?:https?://[^\\s<>\"']+|www\\.[^\\s<>\"']+|(?<![\\w@.-])[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9-]+)*\\.[a-z]{2,}(?:/[^\\s<>\"']*)?)",
)

/**
 * Finds web links inside a text. A link needs "http(s)://", "www." or a real domain ending (like .com).
 * File names (main.py, README.md), e-mail addresses and anything touching an "@" are ignored.
 */
fun String.findUrls(): List<String> {
    val found = mutableListOf<String>()
    for (match in LinkRegex.findAll(this)) {
        var url = match.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'', '"')
        if (url.isEmpty()) continue
        val start = match.range.first
        val end = start + url.length
        if (start > 0 && this[start - 1] == '@') continue
        if (end < length && this[end] == '@') continue
        val lower = url.lowercase()
        val hasScheme = lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.")
        if (!hasScheme) {
            val host = lower.substringBefore('/').substringBefore('?')
            val tld = host.substringAfterLast('.')
            if (tld !in LinkTlds) continue
            if ('@' in url) continue
        }
        found.add(url)
    }
    return found
}
