package com.payra.calling.feature.wallpaper

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.FakeChatStore
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.TextLink
import com.payra.calling.designsystem.WallpaperPalette
import com.payra.calling.designsystem.Wallpapers
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.enterFromBottom

/**
 * Figma screens "64 Wallpaper Picker" and "65 Preview".
 * Opened from a chat: choose "Only this chat" or "All chats". Opened from settings: all chats.
 * Besides the colors you can use a real photo from your gallery.
 */
@Composable
fun WallpaperScreen(contactId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val convo = if (contactId != null) FakeChatStore.conversationFor(contactId) else null
    var scope by remember { mutableIntStateOf(if (convo != null) 0 else 1) } // 0 = this chat only, 1 = all chats

    val applyAll = scope == 1 || convo == null
    val previewPalette = if (!applyAll && convo != null && convo.wallpaper >= 0) Wallpapers.all[convo.wallpaper.coerceIn(0, Wallpapers.all.lastIndex)] else Wallpapers.current
    val previewPhoto = if (!applyAll && convo != null) convo.wallpaperUri.ifEmpty { Wallpapers.customUri } else Wallpapers.customUri
    val selectedIndex = if (!applyAll && convo != null) {
        if (convo.wallpaperUri.isNotEmpty()) -1 else if (convo.wallpaper >= 0) convo.wallpaper else Wallpapers.selected
    } else {
        if (Wallpapers.customUri.isNotEmpty()) -1 else Wallpapers.selected
    }

    fun applyPalette(index: Int) {
        if (!applyAll && convo != null) {
            convo.wallpaper = index
            convo.wallpaperUri = ""
        } else {
            Wallpapers.selected = index
            Wallpapers.customUri = ""
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // fine for this session
            }
            if (!applyAll && convo != null) {
                convo.wallpaperUri = uri.toString()
            } else {
                Wallpapers.customUri = uri.toString()
            }
        }
    }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Chat wallpaper", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(14.dp))

            if (convo != null) {
                GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(1), shape = RoundedCornerShape(22.dp)) {
                    BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                        val half = maxWidth / 2
                        val pillX by animateDpAsState(
                            targetValue = half * scope,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                            label = "scopePill",
                        )
                        Box(
                            modifier = Modifier
                                .offset(x = pillX)
                                .width(half)
                                .height(40.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PayraColors.Primary),
                        )
                        Row {
                            listOf("Only this chat", "All chats").forEachIndexed { index, label ->
                                Box(
                                    modifier = Modifier
                                        .width(half)
                                        .height(40.dp)
                                        .bouncyClick(onClick = { scope = index }, pressedScale = 0.94f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (scope == index) Color.White else PayraColors.Muted,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // Live preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .enterFromBottom(2)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(28.dp)),
            ) {
                GlowBackground(palette = previewPalette, animated = false) {
                    if (previewPhoto.isNotEmpty()) {
                        PayraImage(model = previewPhoto, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.30f)))
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GlassSurface(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)) {
                            Text("Hi! How do you like this wallpaper?", color = PayraColors.Ink, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp))
                                    .background(PayraColors.Primary)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) { Text("Looks great!", color = Color.White, fontSize = 14.sp) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            // Real photo
            GlassSurface(
                modifier = Modifier.fillMaxWidth().enterFromBottom(3),
                shape = RoundedCornerShape(24.dp),
                onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = PayraColors.Primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Choose a photo from your gallery", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink, modifier = Modifier.weight(1f))
                    if (selectedIndex == -1) Icon(Icons.Outlined.Check, contentDescription = "In use", tint = PayraColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "OR PICK A COLOR STYLE",
                color = PayraColors.Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 10.dp).enterFromBottom(4),
            )
            Wallpapers.all.chunked(3).forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEachIndexed { colIndex, palette ->
                        val index = rowIndex * 3 + colIndex
                        WallpaperTile(
                            palette = palette,
                            selected = selectedIndex == index,
                            onClick = { applyPalette(index) },
                            modifier = Modifier.weight(1f).enterFromBottom(5 + index),
                        )
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            if (!applyAll && convo != null && (convo.wallpaper >= 0 || convo.wallpaperUri.isNotEmpty())) {
                TextLink(
                    text = "Use the app wallpaper in this chat",
                    onClick = {
                        convo.wallpaper = -1
                        convo.wallpaperUri = ""
                    },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WallpaperTile(palette: WallpaperPalette, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) PayraColors.Primary else Color.Transparent,
        animationSpec = tween(180),
        label = "wallpaperBorder",
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .bouncyClick(onClick = onClick, pressedScale = 0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(3.dp, borderColor, RoundedCornerShape(20.dp)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(palette.base)
                drawCircle(
                    brush = Brush.radialGradient(listOf(palette.glowA.copy(alpha = 0.5f), Color.Transparent), center = Offset(size.width * 0.1f, size.height * 0.05f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.1f, size.height * 0.05f),
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(palette.glowC.copy(alpha = 0.6f), Color.Transparent), center = Offset(size.width * 0.95f, size.height * 0.95f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.95f, size.height * 0.95f),
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(PayraColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(palette.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) PayraColors.Primary else PayraColors.Muted)
    }
}
