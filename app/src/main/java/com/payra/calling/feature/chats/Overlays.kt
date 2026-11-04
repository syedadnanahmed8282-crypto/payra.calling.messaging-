package com.payra.calling.feature.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

val ReactionEmojis = listOf(
    "\uD83D\uDC4D", "\u2764\uFE0F", "\uD83D\uDE02", "\uD83D\uDE2E", "\uD83D\uDE22", "\uD83D\uDE4F",
    "\uD83D\uDD25", "\uD83C\uDF89", "\uD83D\uDC4F", "\uD83D\uDE0D", "\uD83D\uDE21", "\uD83E\uDD14",
    "\uD83D\uDE0E", "\uD83D\uDCAF", "\uD83E\uDD73", "\uD83D\uDE2D",
)

/** Figma "66 Reaction Picker": quick emoji row. Each emoji pops in one after the other. */
@Composable
fun ReactionPicker(onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier.widthIn(max = 330.dp), shape = RoundedCornerShape(30.dp), fillAlpha = 0.9f) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReactionEmojis.forEachIndexed { index, emoji ->
                Text(
                    text = emoji,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .enterFromBottom(index = index, delayPerItem = 35L, distance = 14.dp)
                        .bouncyClick(onClick = { onPick(emoji) }, pressedScale = 0.75f)
                        .padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/** Top bar while messages are selected: close button + "N selected". */
@Composable
fun SelectionTopBar(count: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassCircleButton(icon = Icons.Outlined.Close, contentDescription = "Cancel selection", onClick = onClose)
        Spacer(Modifier.width(8.dp))
        GlassSurface(modifier = Modifier.weight(1f), shape = PayraShapes.Pill) {
            Text(
                text = "$count selected",
                color = PayraColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .bouncyClick(onClick = onClick, pressedScale = 0.88f)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Replaces the message bar while selecting: Reply, Edit (own text only), Forward, Copy, Delete. */
@Composable
fun SelectionActionBar(
    canReply: Boolean,
    canEdit: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
            if (canReply) ActionItem(Icons.AutoMirrored.Filled.Reply, "Reply", PayraColors.Ink, onReply, Modifier.weight(1f))
            if (canEdit) ActionItem(Icons.Outlined.Edit, "Edit", PayraColors.Ink, onEdit, Modifier.weight(1f))
            ActionItem(Icons.Outlined.Shortcut, "Forward", PayraColors.Ink, onForward, Modifier.weight(1f))
            ActionItem(Icons.Outlined.ContentCopy, "Copy", PayraColors.Ink, onCopy, Modifier.weight(1f))
            ActionItem(Icons.Outlined.Delete, "Delete", PayraColors.Danger, onDelete, Modifier.weight(1f))
        }
    }
}

/** Banner above the message bar: "Replying to ..." or "Edit message". */
@Composable
fun ComposerBanner(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClose: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PayraColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = PayraColors.Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Cancel",
                tint = PayraColors.Muted,
                modifier = Modifier.size(20.dp).bouncyClick(onClick = onClose, pressedScale = 0.8f),
            )
        }
    }
}

/** Dimmed screen + glass sheet that springs up from the bottom (attachments, delete options). */
@Composable
fun BottomSheetOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(220)), exit = fadeOut(tween(180))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) { h -> h } + fadeIn(tween(160)),
            exit = slideOutVertically(animationSpec = tween(220)) { h -> h } + fadeOut(tween(160)),
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                shape = RoundedCornerShape(32.dp),
                fillAlpha = 0.9f,
            ) {
                Column(modifier = Modifier.padding(20.dp), content = content)
            }
        }
    }
}

@Composable
fun SheetOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
) {
    Column(
        modifier = modifier.bouncyClick(onClick = onClick, pressedScale = 0.88f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = PayraColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SheetButton(text: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick, pressedScale = 0.97f)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Row of the three-dot menu in the chat header. */
@Composable
fun MenuRow(icon: ImageVector, label: String, tint: Color = PayraColors.Ink, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick, pressedScale = 0.97f)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
