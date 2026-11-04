package com.payra.calling.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class Contact(val id: String, val name: String, val about: String, val color: Color)

enum class MessageKind { Text, Image, Video, Voice, File, Location, ContactCard }

enum class MessageStatus { Sending, Sent, Delivered, Read, Failed }

/** A fake photo / video (just a colored picture) until real media is connected. [tone] picks the colors. */
data class MediaItem(val tone: Int, val isVideo: Boolean = false, val duration: String = "")

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
    val placeName: String = "",
    val placeAddress: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
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
    }
}

class Conversation(val contact: Contact) {
    val messages = mutableStateListOf<ChatMessage>()
    var unread by mutableIntStateOf(0)
    var pinned by mutableStateOf(false)
    var muted by mutableStateOf(false)
    var archived by mutableStateOf(false)
}
