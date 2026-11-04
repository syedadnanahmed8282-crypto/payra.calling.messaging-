package com.payra.calling.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.payra.calling.designsystem.PayraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Temporary in-memory data so every screen can be tried on the phone. Replaced by Supabase later. */
object FakeChatStore {
    val contacts = mutableStateListOf(
        Contact("ayesha", "Ayesha", "Hey there! I am using Payra", PayraColors.Primary),
        Contact("rahim", "Rahim Bhai", "Busy", Color(0xFFF28B66)),
        Contact("nusrat", "Nusrat", "At work", Color(0xFFE4A93A)),
        Contact("tanvir", "Tanvir", "Sleeping", Color(0xFF3FA796)),
        Contact("farabi", "Farabi Ahmed", "Available", Color(0xFF7B6FD6)),
    )

    val conversations = mutableStateListOf<Conversation>()

    /** Ids of blocked contacts (demo). */
    val blocked = mutableStateListOf("tanvir")

    private var nextId = 1L
    private var demoReplyIndex = 0

    private val demoReplies = listOf(
        "Sounds good!",
        "Haha, nice one",
        "Give me five minutes",
        "Okay, see you there",
        "Can you send me the details?",
        "Perfect, thanks!",
    )

    private fun now(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

    fun contact(id: String): Contact = contacts.first { it.id == id }

    fun conversationFor(contactId: String): Conversation {
        val existing = conversations.firstOrNull { it.contact.id == contactId }
        if (existing != null) return existing
        val created = Conversation(contact(contactId))
        conversations.add(0, created)
        return created
    }

    private fun bump(conversation: Conversation) {
        conversations.remove(conversation)
        conversations.add(0, conversation)
    }

    fun addMessage(conversation: Conversation, make: (id: Long, time: String) -> ChatMessage): ChatMessage {
        val message = make(nextId++, now())
        conversation.messages.add(message)
        bump(conversation)
        return message
    }

    fun update(conversation: Conversation, id: Long, transform: (ChatMessage) -> ChatMessage) {
        val index = conversation.messages.indexOfFirst { it.id == id }
        if (index >= 0) conversation.messages[index] = transform(conversation.messages[index])
    }

    // ---------- sending ----------

    fun send(conversation: Conversation, text: String, replyTo: ReplyRef? = null): ChatMessage =
        addMessage(conversation) { id, t ->
            ChatMessage(id, true, t, text = text, replyTo = replyTo, status = MessageStatus.Sending)
        }

    fun sendMedia(conversation: Conversation, media: List<MediaItem>, caption: String = ""): ChatMessage =
        addMessage(conversation) { id, t ->
            val kind = if (media.size == 1 && media[0].isVideo) MessageKind.Video else MessageKind.Image
            ChatMessage(id, true, t, kind = kind, text = caption, media = media, status = MessageStatus.Sending)
        }

    fun sendVoice(conversation: Conversation, duration: String, audioPath: String = ""): ChatMessage =
        addMessage(conversation) { id, t ->
            ChatMessage(id, true, t, kind = MessageKind.Voice, voiceDuration = duration, audioPath = audioPath, status = MessageStatus.Sending)
        }

    fun sendFile(conversation: Conversation, name: String, info: String): ChatMessage =
        addMessage(conversation) { id, t ->
            ChatMessage(id, true, t, kind = MessageKind.File, fileName = name, fileInfo = info, status = MessageStatus.Sending)
        }

    fun sendLocation(conversation: Conversation, name: String, address: String): ChatMessage =
        addMessage(conversation) { id, t ->
            ChatMessage(id, true, t, kind = MessageKind.Location, placeName = name, placeAddress = address, status = MessageStatus.Sending)
        }

    fun sendContactCard(conversation: Conversation, name: String, phone: String): ChatMessage =
        addMessage(conversation) { id, t ->
            ChatMessage(id, true, t, kind = MessageKind.ContactCard, contactName = name, contactPhone = phone, status = MessageStatus.Sending)
        }

    fun setStatus(conversation: Conversation, id: Long, status: MessageStatus) =
        update(conversation, id) { it.copy(status = status) }

    // ---------- actions ----------

    fun edit(conversation: Conversation, id: Long, newText: String) =
        update(conversation, id) { it.copy(text = newText, edited = true) }

    fun deleteForEveryone(conversation: Conversation, id: Long) =
        update(conversation, id) {
            it.copy(deleted = true, text = "", media = emptyList(), reactions = emptyList(), replyTo = null, forwarded = false)
        }

    fun deleteForMe(conversation: Conversation, id: Long) {
        conversation.messages.removeAll { it.id == id }
    }

    fun clearChat(conversation: Conversation) {
        conversation.messages.clear()
    }

    fun deleteChat(conversation: Conversation) {
        conversations.remove(conversation)
    }

    fun block(contactId: String) {
        if (contactId !in blocked) blocked.add(contactId)
    }

    private val newContactColors = listOf(
        Color(0xFF2B8FAE), Color(0xFFF28B66), Color(0xFF7B6FD6), Color(0xFF3FA796), Color(0xFFE4A93A),
    )

    /** Adds a contact by name (demo: any new contact counts as being on the app). */
    fun addContact(name: String, phone: String, email: String): Contact {
        val contact = Contact(
            id = "c${contacts.size + 1}",
            name = name.ifBlank { phone.ifBlank { email } },
            about = "Hey there! I am using Payra",
            color = newContactColors[contacts.size % newContactColors.size],
        )
        contacts.add(contact)
        return contact
    }

    /** One reaction per person: choosing a new emoji replaces mine, choosing the same one removes it. */
    fun toggleReaction(conversation: Conversation, id: Long, emoji: String) =
        update(conversation, id) { m ->
            val mine = m.reactions.firstOrNull { it.mine }
            var list = m.reactions
            if (mine != null) {
                list = list.mapNotNull { r ->
                    if (r.mine) {
                        if (r.count > 1) r.copy(count = r.count - 1, mine = false) else null
                    } else {
                        r
                    }
                }
            }
            if (mine?.emoji == emoji) {
                m.copy(reactions = list)
            } else {
                val existing = list.firstOrNull { it.emoji == emoji }
                list = if (existing != null) {
                    list.map { if (it.emoji == emoji) it.copy(count = it.count + 1, mine = true) else it }
                } else {
                    list + Reaction(emoji, 1, true)
                }
                m.copy(reactions = list)
            }
        }

    fun forward(from: Conversation, messageIds: List<Long>, targetContactIds: List<String>) {
        val originals = messageIds.mapNotNull { id -> from.messages.firstOrNull { it.id == id } }.filter { !it.deleted }
        targetContactIds.forEach { contactId ->
            val target = conversationFor(contactId)
            originals.forEach { original ->
                addMessage(target) { id, t ->
                    original.copy(
                        id = id,
                        time = t,
                        sent = true,
                        forwarded = true,
                        replyTo = null,
                        edited = false,
                        reactions = emptyList(),
                        status = MessageStatus.Sent,
                    )
                }
            }
        }
    }

    /** DEMO ONLY: a fake incoming message so the "message received" animation can be seen. */
    fun receiveDemo(conversation: Conversation) {
        val text = demoReplies[demoReplyIndex % demoReplies.size]
        demoReplyIndex++
        addMessage(conversation) { id, t -> ChatMessage(id, false, t, text = text) }
    }

    // ---------- seed data ----------

    private fun seedText(c: Conversation, sent: Boolean, text: String, time: String): ChatMessage {
        val m = ChatMessage(nextId++, sent, time, text = text)
        c.messages.add(m)
        return m
    }

    init {
        val ayesha = Conversation(contact("ayesha"))
        seedText(ayesha, false, "Hey! Are you free tonight?", "9:30 PM")
        seedText(ayesha, true, "Yes, what is the plan?", "9:31 PM")
        seedText(ayesha, false, "Dinner at 8? Mom is cooking", "9:32 PM")
        seedText(ayesha, true, "Perfect, I will be there", "9:33 PM")
        val photos = ChatMessage(
            nextId++, false, "9:36 PM", kind = MessageKind.Image, text = "Weekend trip photos",
            media = listOf(MediaItem(0), MediaItem(3), MediaItem(1), MediaItem(4), MediaItem(2), MediaItem(5)),
        )
        ayesha.messages.add(photos)
        ayesha.messages.add(
            ChatMessage(
                nextId++, true, "9:37 PM", text = "Love these!",
                replyTo = ReplyRef(photos.id, "Ayesha", "6 photos"),
                reactions = listOf(Reaction("\u2764\uFE0F", 1, false)),
            ),
        )
        ayesha.messages.add(ChatMessage(nextId++, true, "9:38 PM", kind = MessageKind.Video, media = listOf(MediaItem(1, true, "0:24"))))
        ayesha.messages.add(
            ChatMessage(nextId++, false, "9:40 PM", kind = MessageKind.Voice, voiceDuration = "0:18",
                reactions = listOf(Reaction("\uD83D\uDC4D", 1, false))),
        )
        ayesha.messages.add(ChatMessage(nextId++, true, "9:40 PM", kind = MessageKind.Voice, voiceDuration = "0:09"))
        ayesha.messages.add(ChatMessage(nextId++, false, "9:41 PM", kind = MessageKind.File, fileName = "Project_Plan.pdf", fileInfo = "2.4 MB  PDF"))
        ayesha.messages.add(ChatMessage(nextId++, true, "9:41 PM", kind = MessageKind.Location, placeName = "Current location", placeAddress = "Dhaka, Bangladesh"))
        ayesha.messages.add(ChatMessage(nextId++, false, "9:42 PM", kind = MessageKind.ContactCard, contactName = "Nusrat", contactPhone = "+880 1XXX XXXXXX"))
        ayesha.messages.add(ChatMessage(nextId++, false, "9:42 PM", text = "Great, see you soon", forwarded = false))
        ayesha.unread = 2
        conversations.add(ayesha)

        fun simple(contactId: String, unread: Int, time: String, text: String) {
            val c = Conversation(contact(contactId))
            c.messages.add(ChatMessage(nextId++, false, time, text = text))
            c.unread = unread
            conversations.add(c)
        }
        simple("rahim", 0, "8:12 PM", "See you tomorrow!")
        simple("nusrat", 0, "Yesterday", "Thanks a lot")
        simple("tanvir", 1, "Mon", "Missed voice call")
    }
}
