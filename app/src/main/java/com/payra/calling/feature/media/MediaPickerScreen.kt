package com.payra.calling.feature.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.data.DeviceData
import com.payra.calling.data.GalleryItem
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.IllustrationCircle
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.bouncyClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Your real photos and videos. Tabs All / Photos / Videos, tap to select (numbered),
 * long-press to preview big. Needs the gallery permission, which is asked here.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaPickerScreen(
    onBack: () -> Unit,
    onSend: (List<MediaItem>) -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(DeviceData.hasGalleryPermission(context)) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var tab by remember { mutableIntStateOf(0) }
    val chosen = remember { mutableStateListOf<GalleryItem>() }
    var preview by remember { mutableStateOf<GalleryItem?>(null) }
    val tabs = listOf("All", "Photos", "Videos")

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermission = DeviceData.hasGalleryPermission(context)
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loading = true
            items = withContext(Dispatchers.IO) { DeviceData.loadGallery(context) }
            loading = false
        } else {
            loading = false
        }
    }

    val shown = when (tab) {
        1 -> items.filter { !it.isVideo }
        2 -> items.filter { it.isVideo }
        else -> items
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlassScaffold(
            topBar = {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ScreenHeader(title = "Photos and videos", onBack = onBack)
                    Spacer(Modifier.height(10.dp))
                    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                        BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                            val tabWidth = maxWidth / tabs.size
                            val pillX by animateDpAsState(
                                targetValue = tabWidth * tab,
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                                label = "pickerTab",
                            )
                            Box(
                                modifier = Modifier
                                    .offset(x = pillX)
                                    .width(tabWidth)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PayraColors.Primary),
                            )
                            Row {
                                tabs.forEachIndexed { index, label ->
                                    Box(
                                        modifier = Modifier
                                            .width(tabWidth)
                                            .height(40.dp)
                                            .bouncyClick(onClick = { tab = index }, pressedScale = 0.94f),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (tab == index) Color.White else PayraColors.Muted,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = chosen.isNotEmpty(),
                    enter = fadeIn(tween(160)) + scaleIn(spring(dampingRatio = 0.7f, stiffness = 400f), initialScale = 0.85f),
                    exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.9f),
                ) {
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(32.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${chosen.size} selected",
                                color = PayraColors.Ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .bouncyClick(
                                        onClick = {
                                            onSend(chosen.map { MediaItem(0, it.isVideo, if (it.isVideo) it.durationLabel else "", it.uri.toString()) })
                                        },
                                        pressedScale = 0.88f,
                                    )
                                    .clip(CircleShape)
                                    .background(PayraColors.Primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            },
        ) { padding ->
            when {
                !hasPermission -> Column(
                    modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    IllustrationCircle(icon = Icons.Outlined.PhotoLibrary)
                    Spacer(Modifier.height(16.dp))
                    Text("Allow access to your photos and videos", color = PayraColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("Payra only shows what you pick.", color = PayraColors.Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    PayraPrimaryButton(text = "Allow access", onClick = { permissionLauncher.launch(DeviceData.galleryPermissions()) })
                }
                !loading && shown.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Nothing here yet", color = PayraColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("No photos or videos were found.", color = PayraColors.Muted, fontSize = 14.sp)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        end = 4.dp,
                        top = padding.calculateTopPadding() + 4.dp,
                        bottom = padding.calculateBottomPadding() + 4.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(shown, key = { it.uri.toString() }) { item ->
                        val order = chosen.indexOf(item) + 1
                        val isChosen = order > 0
                        val borderColor by animateColorAsState(
                            targetValue = if (isChosen) PayraColors.Primary else Color.Transparent,
                            animationSpec = tween(160),
                            label = "pickBorder",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onLongClick = { preview = item },
                                    onClick = {
                                        if (isChosen) {
                                            chosen.remove(item)
                                        } else {
                                            chosen.add(item)
                                        }
                                    },
                                )
                                .border(3.dp, borderColor, RoundedCornerShape(12.dp)),
                        ) {
                            PayraImage(model = item.uri, modifier = Modifier.fillMaxSize())
                            if (item.isVideo) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.45f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(item.durationLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) PayraColors.Primary else Color.Black.copy(alpha = 0.25f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isChosen) Text(order.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Big preview (long-press a picture)
        AnimatedVisibility(
            visible = preview != null,
            enter = fadeIn(tween(180)) + scaleIn(spring(dampingRatio = 0.8f, stiffness = 350f), initialScale = 0.9f),
            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.95f),
        ) {
            val item = preview
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.94f))) {
                    PayraImage(model = item.uri, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    if (item.isVideo) {
                        Box(
                            modifier = Modifier.align(Alignment.Center).size(72.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassCircleButton(icon = Icons.Outlined.Close, contentDescription = "Close preview", onClick = { preview = null })
                    }
                    val isChosen = item in chosen
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp)) {
                        PayraPrimaryButton(
                            text = if (isChosen) "Selected - tap to remove" else "Select",
                            modifier = Modifier.width(260.dp),
                            onClick = {
                                if (isChosen) {
                                    chosen.remove(item)
                                } else {
                                    chosen.add(item)
                                }
                                preview = null
                            },
                        )
                    }
                }
            }
        }
    }
}
