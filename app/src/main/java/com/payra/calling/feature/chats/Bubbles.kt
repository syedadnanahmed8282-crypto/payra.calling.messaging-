package com.payra.calling.feature.chats

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.ChatMessage
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MessageKind
import com.payra.calling.data.MessageStatus
import com.payra.calling.data.Reaction
import com.payra.calling.data.ReplyRef
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------------------------
// Fake pictures (colored landscapes) used until real photos / videos are connected
// ---------------------------------------------------------------------------------------------

private val Tones = listOf(
    listOf(Color(0xFF8ED3E4), Color(0xFF3FA796), Color(0xFF1B6F8A)),
    listOf(Color(0xFFFFB27C), Color(0xFFD6457B), Color(0xFF6B2D7C)),
    listOf(Color(0xFFC3B5F5), Color(0xFF7B6FD6), Color(0xFF4B3FA0)),
    listOf(Color(0xFFA8E6C4), Color(0xFF3FA796), Color(0xFF1E6F5C)),
    listOf(Color(0xFFF7D98A), Color(0xFFF28B66), Color(0xFFC2417A)),
    listOf(Color(0xFF9CCBEF), Color(0xFF2B8FAE), Color(0xFF0E4A5F)),
)

@Composable
fun MediaTile(item: MediaItem, modifier: Modifier = Modifier, moreCount: Int = 0, onClick: (() -> Unit)? = null) {
    val colors = Tones[item.tone % Tones.size]
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(colors[0])
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = size.minDimension * 0.14f,
                center = Offset(size.width * 0.74f, size.height * 0.22f),
            )
            drawOval(
                color = colors[1].copy(alpha = 0.85f),
                topLeft = Offset(-size.width * 0.25f, size.height * 0.5f),
                size = Size(size.width * 1.2f, size.height * 0.7f),
            )
            drawOval(
                color = colors[2].copy(alpha = 0.9f),
                topLeft = Offset(size.width * 0.3f, size.height * 0.62f),
                size = Size(size.width, size.height * 0.6f),
            )
        }
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            if (item.duration.isNotEmpty()) {
                Text(
                    text = item.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        if (moreCount > 0) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$moreCount", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Figma "69 Multiple Media Bubbles": 1, 2, 3 or 4+ pictures. */
@Composable
fun MediaGrid(media: List<MediaItem>, onOpen: (MediaItem) -> Unit = {}) {
    when (media.size) {
        0 -> Unit
        1 -> MediaTile(media[0], Modifier.size(width = 230.dp, height = 170.dp), onClick = { onOpen(media[0]) })
        2 -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MediaTile(media[0], Modifier.size(width = 112.dp, height = 140.dp), onClick = { onOpen(media[0]) })
            MediaTile(media[1], Modifier.size(width = 112.dp, height = 140.dp), onClick = { onOpen(media[1]) })
        }
        3 -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MediaTile(media[0], Modifier.size(width = 112.dp, height = 188.dp), onClick = { onOpen(media[0]) })
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MediaTile(media[1], Modifier.size(width = 112.dp, height = 92.dp), onClick = { onOpen(media[1]) })
                MediaTile(media[2], Modifier.size(width = 112.dp, height = 92.dp), onClick = { onOpen(media[2]) })
            }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MediaTile(media[0], Modifier.size(112.dp), onClick = { onOpen(media[0]) })
                MediaTile(media[1], Modifier.size(112.dp), onClick = { onOpen(media[1]) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MediaTile(media[2], Modifier.size(112.dp), onClick = { onOpen(media[2]) })
                MediaTile(media[3], Modifier.size(112.dp), moreCount = (media.size - 4).coerceAtLeast(0), onClick = { onOpen(media[3]) })
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Voice message: play / pause, waveform fills while playing
// ---------------------------------------------------------------------------------------------

private val WaveHeights = floatArrayOf(
    0.30f, 0.50f, 0.38f, 0.75f, 1.0f, 0.60f, 0.85f, 0.45f, 0.68f, 0.92f,
    0.52f, 0.30f, 0.78f, 1.0f, 0.70f, 0.38f, 0.60f, 0.85f, 0.45f, 0.30f,
)

private fun parseSeconds(duration: String): Int {
    val parts = duration.split(":")
    val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return (minutes * 60 + seconds).coerceAtLeast(1)
}

@Composable
private fun VoiceContent(duration: String, sent: Boolean, audioPath: String) {
    val total = remember(duration) { parseSeconds(duration) }
    var playing by remember { mutableStateOf(false) }
    var prepared by remember(audioPath) { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val player = remember(audioPath) { if (audioPath.isNotEmpty()) MediaPlayer() else null }

    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    LaunchedEffect(playing) {
        val p = player
        if (!playing) {
            if (p != null && prepared && p.isPlaying) p.pause()
            return@LaunchedEffect
        }
        if (p != null) {
            // A really recorded voice message: play it with the media player.
            try {
                if (!prepared) {
                    p.reset()
                    p.setDataSource(audioPath)
                    p.prepare()
                    prepared = true
                }
                val totalMs = p.duration.coerceAtLeast(1)
                p.start()
                while (p.isPlaying) {
                    progress.snapTo((p.currentPosition.toFloat() / totalMs).coerceIn(0f, 1f))
                    delay(50)
                }
                if (playing) {
                    playing = false
                    progress.snapTo(0f)
                    p.seekTo(0)
                }
            } catch (e: Exception) {
                playing = false
            }
        } else {
            // A demo voice message: just animate the waveform.
            val remaining = (1f - progress.value).coerceAtLeast(0.02f)
            progress.animateTo(1f, tween((total * 1000 * remaining).toInt().coerceAtLeast(300), easing = LinearEasing))
            playing = false
            progress.snapTo(0f)
        }
    }

    val fg = if (sent) Color.White else PayraColors.Primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .bouncyClick(onClick = { playing = !playing }, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(if (sent) Color.White.copy(alpha = 0.22f) else PayraColors.Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = playing,
                transitionSpec = {
                    (fadeIn(tween(120)) + scaleIn(initialScale = 0.5f)) togetherWith (fadeOut(tween(80)) + scaleOut(targetScale = 0.5f))
                },
                label = "playPause",
            ) { isPlaying ->
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = fg,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Canvas(modifier = Modifier.width(120.dp).height(28.dp)) {
            val count = WaveHeights.size
            val gap = size.width / count
            val barWidth = gap * 0.55f
            WaveHeights.forEachIndexed { index, h ->
                val barHeight = size.height * h
                val played = (index + 0.5f) / count <= progress.value
                drawRoundRect(
                    color = fg.copy(alpha = if (played) 1f else 0.4f),
                    topLeft = Offset(index * gap + (gap - barWidth) / 2f, (size.height - barHeight) / 2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(duration, color = fg.copy(alpha = 0.9f), fontSize = 12.sp)
    }
}

// ---------------------------------------------------------------------------------------------
// File, location, contact cards
// ---------------------------------------------------------------------------------------------

@Composable
private fun FileContent(name: String, info: String, sent: Boolean) {
    val fg = if (sent) Color.White else PayraColors.Ink
    val sub = if (sent) Color.White.copy(alpha = 0.8f) else PayraColors.Muted
    val tile = if (sent) Color.White.copy(alpha = 0.2f) else PayraColors.Primary.copy(alpha = 0.12f)
    val tint = if (sent) Color.White else PayraColors.Primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(info, color = sub, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.FileDownload, contentDescription = "Download", tint = tint, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun LocationContent(name: String, address: String, sent: Boolean) {
    val fg = if (sent) Color.White else PayraColors.Ink
    val sub = if (sent) Color.White.copy(alpha = 0.8f) else PayraColors.Muted
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(width = 230.dp, height = 120.dp).clip(RoundedCornerShape(16.dp))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Color(0xFFE3F1E8))
                drawOval(
                    color = Color(0xFFCDEAF5),
                    topLeft = Offset(-size.width * 0.1f, size.height * 0.55f),
                    size = Size(size.width * 1.2f, size.height * 0.5f),
                )
                val road = Color.White.copy(alpha = 0.9f)
                drawRect(road, Offset(0f, size.height * 0.33f), Size(size.width, size.height * 0.08f))
                drawRect(road, Offset(0f, size.height * 0.70f), Size(size.width, size.height * 0.07f))
                drawRect(road, Offset(size.width * 0.30f, 0f), Size(size.width * 0.04f, size.height))
                drawRect(road, Offset(size.width * 0.70f, 0f), Size(size.width * 0.04f, size.height))
            }
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = PayraColors.Danger,
                modifier = Modifier.align(Alignment.Center).size(38.dp),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 2.dp)) {
            Text(name, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(address, color = sub, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ContactCardContent(name: String, phone: String, sent: Boolean) {
    val fg = if (sent) Color.White else PayraColors.Ink
    val sub = if (sent) Color.White.copy(alpha = 0.8f) else PayraColors.Muted
    val chip = if (sent) Color.White.copy(alpha = 0.2f) else PayraColors.Primary.copy(alpha = 0.12f)
    val chipText = if (sent) Color.White else PayraColors.Primary
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(initial = name.take(1), color = if (sent) Color.White.copy(alpha = 0.25f) else Color(0xFFE4A93A), size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(phone, color = sub, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Message", "Add contact").forEach { label ->
                Text(
                    text = label,
                    color = chipText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(chip)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Pieces shared by all bubbles
// ---------------------------------------------------------------------------------------------

@Composable
private fun QuoteBlock(reply: ReplyRef, sent: Boolean, onClick: () -> Unit) {
    val bg = if (sent) Color.White.copy(alpha = 0.18f) else PayraColors.Primary.copy(alpha = 0.10f)
    val bar = if (sent) Color.White else PayraColors.Primary
    val nameColor = if (sent) Color.White else PayraColors.Primary
    val textColor = if (sent) Color.White.copy(alpha = 0.85f) else PayraColors.Ink.copy(alpha = 0.85f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .bouncyClick(onClick = onClick, pressedScale = 0.97f)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(3.dp).height(30.dp).clip(RoundedCornerShape(2.dp)).background(bar))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(reply.authorName, color = nameColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(reply.preview, color = textColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Sending (clock) -> Sent (tick) -> Delivered (double tick) -> Read (blue double tick), with a pop between states. */
@Composable
fun StatusTicks(status: MessageStatus) {
    AnimatedContent(
        targetState = status,
        transitionSpec = {
            (scaleIn(animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f), initialScale = 0.4f) + fadeIn(tween(120))) togetherWith
                (scaleOut(targetScale = 0.4f) + fadeOut(tween(80)))
        },
        label = "ticks",
    ) { s ->
        val icon = when (s) {
            MessageStatus.Sending -> Icons.Outlined.Schedule
            MessageStatus.Sent -> Icons.Outlined.Done
            MessageStatus.Failed -> Icons.Outlined.ErrorOutline
            else -> Icons.Outlined.DoneAll
        }
        val tint = when (s) {
            MessageStatus.Read -> Color(0xFF9BE7FF)
            MessageStatus.Failed -> Color(0xFFFFB4B6)
            else -> Color.White.copy(alpha = 0.8f)
        }
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
    }
}

/** Everything inside a bubble: forwarded label, quote, content, caption, footer (edited, time, ticks). */
@Composable
fun BubbleBody(
    message: ChatMessage,
    onQuoteClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit = {},
) {
    val sent = message.sent
    val fg = if (sent) Color.White else PayraColors.Ink
    val sub = if (sent) Color.White.copy(alpha = 0.8f) else PayraColors.Muted

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (message.deleted) {
            Text(
                text = "This message was deleted",
                color = sub,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
            )
        } else {
            if (message.forwarded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = sub, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Forwarded", color = sub, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                }
            }
            message.replyTo?.let { reply ->
                QuoteBlock(reply = reply, sent = sent, onClick = { onQuoteClick(reply.messageId) })
            }
            when (message.kind) {
                MessageKind.Text -> Text(message.text, color = fg, fontSize = 15.sp)
                MessageKind.Image, MessageKind.Video -> {
                    MediaGrid(message.media, onOpen = onOpenMedia)
                    if (message.text.isNotBlank()) {
                        Text(message.text, color = fg, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
                MessageKind.Voice -> VoiceContent(message.voiceDuration, sent, message.audioPath)
                MessageKind.File -> FileContent(message.fileName, message.fileInfo, sent)
                MessageKind.Location -> LocationContent(message.placeName, message.placeAddress, sent)
                MessageKind.ContactCard -> ContactCardContent(message.contactName, message.contactPhone, sent)
            }
        }

        Row(
            modifier = Modifier.align(Alignment.End).padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (message.edited && !message.deleted) {
                Text("Edited", color = sub, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.width(6.dp))
            }
            Text(message.time, color = sub, fontSize = 11.sp)
            if (sent && !message.deleted) {
                Spacer(Modifier.width(3.dp))
                StatusTicks(message.status)
            }
        }
        if (sent && message.status == MessageStatus.Failed) {
            Text(
                text = "Not sent. Tap to retry",
                color = Color(0xFFFFB4B6),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.End).clickable(onClick = onRetry),
            )
        }
    }
}

/** Emoji reactions under a bubble. Tap a chip to add or remove your own reaction. */
@Composable
fun ReactionChips(reactions: List<Reaction>, onTap: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { reaction ->
            GlassSurface(
                shape = RoundedCornerShape(14.dp),
                fillAlpha = if (reaction.mine) 0.95f else 0.7f,
                onClick = { onTap(reaction.emoji) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(reaction.emoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = reaction.count.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reaction.mine) PayraColors.Primary else PayraColors.Ink,
                    )
                }
            }
        }
    }
}
