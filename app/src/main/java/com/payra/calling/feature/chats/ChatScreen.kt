package com.payra.calling.feature.chats

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.payra.calling.data.ChatMessage
import com.payra.calling.data.Contact
import com.payra.calling.data.DeviceData
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MessageKind
import com.payra.calling.data.MessageStatus
import com.payra.calling.data.ReplyRef
import com.payra.calling.data.preview
import com.payra.calling.designsystem.AvatarCircle
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraShapes
import com.payra.calling.designsystem.Wallpapers
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.popIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private enum class BannerKind { Reply, Edit }

/** Where the selected message sits in the list (used to put the emoji row right next to it). */
private data class SelectedSpot(val offset: Int, val size: Int, val sent: Boolean)

/**
 * The full chat. Messages scroll under the glass bars. Long-press selects (emoji row next to the message,
 * reply, edit, forward, copy, delete), double-tap hearts a message, swipe right replies, the first tap on the
 * call icon opens audio / video choices, the mic records a real voice message, the paperclip sends real
 * photos, videos, files, contacts and locations. All sheets are drawn above the message bar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    contactId: String,
    onBack: () -> Unit,
    onForward: (List<Long>) -> Unit,
    onCall: (Boolean) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    onOpenPicker: () -> Unit,
    onOpenContactPicker: () -> Unit,
    onOpenShareLocation: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSharedMedia: () -> Unit,
    onOpenWallpaper: () -> Unit,
) {
    val convo = remember(contactId) { FakeChatStore.conversationFor(contactId) }
    val baselineId = remember(contactId) { convo.messages.lastOrNull()?.id ?: 0L }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = convo.messages.size)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var typing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<Long>() }
    var replyTarget by remember { mutableStateOf<ChatMessage?>(null) }
    var editTarget by remember { mutableStateOf<ChatMessage?>(null) }
    var shownBanner by remember { mutableStateOf<ChatMessage?>(null) }
    var showAttach by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    var showBlock by remember { mutableStateOf(false) }
    var showReactionSheet by remember { mutableStateOf(false) }
    var emojiOpen by remember { mutableStateOf(false) }
    var callExpanded by remember { mutableStateOf(false) }
    var lastSpot by remember { mutableStateOf<SelectedSpot?>(null) }

    val selecting = selected.isNotEmpty()
    val selectedMessages = convo.messages.filter { it.id in selected }
    val bannerMessage = editTarget ?: replyTarget
    val bannerKind = if (editTarget != null) BannerKind.Edit else BannerKind.Reply
    val palette = if (convo.wallpaper >= 0) Wallpapers.all[convo.wallpaper.coerceIn(0, Wallpapers.all.lastIndex)] else Wallpapers.current

    LaunchedEffect(contactId) { convo.unread = 0 }
    LaunchedEffect(bannerMessage?.id) { if (bannerMessage != null) shownBanner = bannerMessage }
    LaunchedEffect(editTarget?.id) { editTarget?.let { draft = it.text } }
    LaunchedEffect(callExpanded) {
        if (callExpanded) {
            delay(4500)
            callExpanded = false
        }
    }
    LaunchedEffect(draft) { if (draft.isNotEmpty()) callExpanded = false }

    val itemCount = 1 + convo.messages.size + (if (typing) 1 else 0)
    LaunchedEffect(itemCount, emojiOpen) { listState.animateScrollToItem(itemCount - 1) }
    val showJump by remember { derivedStateOf { listState.canScrollForward } }

    // Position of the single selected message, so the emoji row can sit right next to it.
    val spot by remember {
        derivedStateOf {
            val id = if (selected.size == 1) selected[0] else null
            if (id == null) {
                null
            } else {
                val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id }
                val sent = convo.messages.firstOrNull { it.id == id }?.sent ?: false
                if (info != null) SelectedSpot(info.offset, info.size, sent) else null
            }
        }
    }
    LaunchedEffect(spot) { if (spot != null) lastSpot = spot }

    BackHandler(
        enabled = selecting || showAttach || showDelete || showMenu || showClear || showBlock ||
            showReactionSheet || emojiOpen || callExpanded || bannerMessage != null,
    ) {
        when {
            showReactionSheet -> showReactionSheet = false
            showDelete -> showDelete = false
            showClear -> showClear = false
            showBlock -> showBlock = false
            showAttach -> showAttach = false
            showMenu -> showMenu = false
            emojiOpen -> emojiOpen = false
            callExpanded -> callExpanded = false
            selecting -> selected.clear()
            else -> {
                editTarget = null
                replyTarget = null
                draft = ""
            }
        }
    }

    // Status ticks move on after sending, then the demo contact "types" and answers (DEMO ONLY).
    fun runDelivery(id: Long) {
        scope.launch {
            delay(350)
            FakeChatStore.setStatus(convo, id, MessageStatus.Sent)
            delay(600)
            FakeChatStore.setStatus(convo, id, MessageStatus.Delivered)
            delay(500)
            FakeChatStore.setStatus(convo, id, MessageStatus.Read)
            typing = true
            delay(1500)
            typing = false
            FakeChatStore.receiveDemo(convo)
        }
    }

    fun sendText(text: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val target = editTarget
        if (target != null) {
            FakeChatStore.edit(convo, target.id, text)
            editTarget = null
            draft = ""
            return
        }
        val reply = replyTarget?.let { ReplyRef(it.id, if (it.sent) "You" else convo.contact.name, it.preview()) }
        val message = FakeChatStore.send(convo, text, reply)
        replyTarget = null
        draft = ""
        runDelivery(message.id)
    }

    fun sendMediaItems(items: List<MediaItem>) {
        val message = FakeChatStore.sendMedia(convo, items)
        FakeChatStore.simulateDelivery(convo, message.id, 2200L)
    }

    // ---------- real camera, video and document ----------
    var captureUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCamera by remember { mutableIntStateOf(0) }

    fun newCaptureUri(extension: String): Uri {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.$extension")
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = captureUri
        if (ok && uri != null) sendMediaItems(listOf(MediaItem(0, false, "", uri.toString())))
    }
    val recordVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val uri = captureUri
        if (ok && uri != null) {
            sendMediaItems(listOf(MediaItem(0, true, DeviceData.videoDurationLabel(context, uri), uri.toString())))
        }
    }
    val pickDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val (name, info) = DeviceData.fileInfo(context, uri)
            val message = FakeChatStore.sendFile(convo, name, info, uri.toString())
            FakeChatStore.simulateDelivery(convo, message.id, 2200L)
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (pendingCamera == 1) {
                val uri = newCaptureUri("jpg")
                captureUri = uri
                takePhoto.launch(uri)
            } else if (pendingCamera == 2) {
                val uri = newCaptureUri("mp4")
                captureUri = uri
                recordVideo.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission is needed", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera(kind: Int) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = newCaptureUri(if (kind == 1) "jpg" else "mp4")
            captureUri = uri
            if (kind == 1) takePhoto.launch(uri) else recordVideo.launch(uri)
        } else {
            pendingCamera = kind
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    fun toggleSelect(id: Long) {
        if (id in selected) selected.remove(id) else selected.add(id)
    }

    val canReply = selectedMessages.size == 1 && !selectedMessages[0].deleted
    val canEdit = selectedMessages.size == 1 && selectedMessages[0].sent &&
        !selectedMessages[0].deleted && selectedMessages[0].kind == MessageKind.Text
    val canDeleteForEveryone = selectedMessages.isNotEmpty() && selectedMessages.all { it.sent && !it.deleted }

    Box(modifier = Modifier.fillMaxSize()) {
        GlassScaffold(
            imeAware = true,
            wallpaper = palette,
            wallpaperUri = convo.wallpaperUri.ifEmpty { Wallpapers.customUri },
            topBar = {
                AnimatedContent(
                    targetState = selecting,
                    transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                    label = "topBar",
                ) { isSelecting ->
                    if (isSelecting) {
                        SelectionTopBar(count = selected.size, onClose = { selected.clear() })
                    } else {
                        ChatHeader(
                            contact = convo.contact,
                            typing = typing,
                            muted = convo.muted,
                            callExpanded = callExpanded,
                            onBack = onBack,
                            onCallTap = { callExpanded = true },
                            onAudioCall = {
                                callExpanded = false
                                onCall(false)
                            },
                            onVideoCall = {
                                callExpanded = false
                                onCall(true)
                            },
                            onProfileTap = {
                                if (callExpanded) {
                                    callExpanded = false
                                } else {
                                    onOpenProfile()
                                }
                            },
                            onMenu = {
                                callExpanded = false
                                showMenu = true
                            },
                        )
                    }
                }
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = bannerMessage != null && !selecting,
                        enter = fadeIn(tween(160)) + expandVertically(spring(dampingRatio = 0.8f, stiffness = 400f)),
                        exit = fadeOut(tween(120)) + shrinkVertically(),
                    ) {
                        val banner = shownBanner
                        if (banner != null) {
                            ComposerBanner(
                                icon = if (bannerKind == BannerKind.Edit) Icons.Outlined.Edit else Icons.AutoMirrored.Filled.Reply,
                                title = if (bannerKind == BannerKind.Edit) "Edit message" else "Replying to ${if (banner.sent) "yourself" else convo.contact.name}",
                                subtitle = banner.preview(),
                                onClose = {
                                    if (editTarget != null) draft = ""
                                    editTarget = null
                                    replyTarget = null
                                },
                            )
                        }
                    }
                    AnimatedContent(
                        targetState = selecting,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "bottomBar",
                    ) { isSelecting ->
                        if (isSelecting) {
                            SelectionActionBar(
                                canReply = canReply,
                                canEdit = canEdit,
                                onReply = {
                                    replyTarget = selectedMessages.firstOrNull()
                                    editTarget = null
                                    selected.clear()
                                },
                                onEdit = {
                                    editTarget = selectedMessages.firstOrNull()
                                    replyTarget = null
                                    selected.clear()
                                },
                                onForward = {
                                    val ids = selected.toList()
                                    selected.clear()
                                    onForward(ids)
                                },
                                onCopy = {
                                    val text = selectedMessages.joinToString("\n") { it.preview() }
                                    clipboard.setText(AnnotatedString(text))
                                    selected.clear()
                                },
                                onDelete = { showDelete = true },
                            )
                        } else {
                            ChatComposer(
                                draft = draft,
                                onDraftChange = { draft = it },
                                isEditing = editTarget != null,
                                emojiOpen = emojiOpen,
                                onEmojiOpenChange = { emojiOpen = it },
                                onSend = { sendText(it) },
                                onSendVoice = { duration, path ->
                                    val m = FakeChatStore.sendVoice(convo, duration, path)
                                    runDelivery(m.id)
                                },
                                onAttach = {
                                    emojiOpen = false
                                    showAttach = true
                                },
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = emojiOpen && !selecting,
                        enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 400f)) + fadeIn(tween(160)),
                        exit = shrinkVertically() + fadeOut(tween(120)),
                    ) {
                        EmojiPanel(
                            onPick = { emoji -> draft += emoji },
                            onBackspace = { draft = draft.dropLastEmojiUnit() },
                            height = 290.dp,
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "date") { DateChip("Today") }
                    items(convo.messages, key = { it.id }) { message ->
                        if (message.kind == MessageKind.CallLog) {
                            CallLogRow(message = message, onCallBack = { onCall(message.callVideo) })
                        } else {
                            MessageRow(
                                message = message,
                                animateIn = message.id > baselineId,
                                selecting = selecting,
                                selected = message.id in selected,
                                onLongClick = {
                                    callExpanded = false
                                    emojiOpen = false
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (message.id !in selected) selected.add(message.id)
                                },
                                onClick = {
                                    if (selecting) {
                                        toggleSelect(message.id)
                                    } else {
                                        emojiOpen = false
                                    }
                                },
                                onSwipeReply = {
                                    if (!message.deleted) {
                                        replyTarget = message
                                        editTarget = null
                                    }
                                },
                                onQuoteClick = { id ->
                                    val index = convo.messages.indexOfFirst { it.id == id }
                                    if (index >= 0) scope.launch { listState.animateScrollToItem(index + 1) }
                                },
                                onReact = { emoji -> FakeChatStore.toggleReaction(convo, message.id, emoji) },
                                onOpenMedia = onOpenMedia,
                                onRetry = {
                                    FakeChatStore.setStatus(convo, message.id, MessageStatus.Sending)
                                    FakeChatStore.simulateDelivery(convo, message.id, 800L)
                                },
                            )
                        }
                    }
                    if (typing) {
                        item(key = "typing") { TypingBubble() }
                    }
                }

                // Any tap on the screen closes the expanded call buttons.
                if (callExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { callExpanded = false },
                    )
                }

                // Emoji row sits right next to the selected message (above it, or below it when there is no room).
                val shownSpot = lastSpot
                val pickerHeightPx = with(density) { 64.dp.toPx() }
                val gapPx = with(density) { 6.dp.toPx() }
                val topLimitPx = with(density) { padding.calculateTopPadding().toPx() }
                AnimatedVisibility(
                    visible = selected.size == 1 && shownSpot != null,
                    modifier = Modifier
                        .align(if (shownSpot?.sent == true) Alignment.TopEnd else Alignment.TopStart)
                        .padding(horizontal = 12.dp)
                        .offset {
                            val s = shownSpot
                            if (s == null) {
                                IntOffset(0, 0)
                            } else {
                                val above = s.offset - pickerHeightPx - gapPx
                                val y = if (above >= topLimitPx) above else s.offset + s.size + gapPx
                                IntOffset(0, y.roundToInt())
                            }
                        },
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f), initialScale = 0.6f) + fadeIn(tween(160)),
                    exit = scaleOut(targetScale = 0.7f) + fadeOut(tween(120)),
                ) {
                    ReactionPicker(
                        onPick = { emoji ->
                            selected.firstOrNull()?.let { FakeChatStore.toggleReaction(convo, it, emoji) }
                            selected.clear()
                        },
                        onMore = { showReactionSheet = true },
                    )
                }

                AnimatedVisibility(
                    visible = showJump && !selecting,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = padding.calculateBottomPadding() + 12.dp),
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f), initialScale = 0.4f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.4f) + fadeOut(),
                ) {
                    GlassCircleButton(
                        icon = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Latest messages",
                        onClick = { scope.launch { listState.animateScrollToItem(itemCount - 1) } },
                    )
                }
            }
        }

        // ================= Everything below is drawn ABOVE the message bar =================

        // Three-dot menu
        AnimatedVisibility(visible = showMenu, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showMenu = false },
            )
        }
        AnimatedVisibility(
            visible = showMenu,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusTop + 64.dp, end = 16.dp),
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f), initialScale = 0.7f, transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(tween(140)),
            exit = scaleOut(targetScale = 0.8f, transformOrigin = TransformOrigin(1f, 0f)) + fadeOut(tween(100)),
        ) {
            GlassSurface(modifier = Modifier.width(250.dp), shape = RoundedCornerShape(24.dp), fillAlpha = 0.94f) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    MenuRow(Icons.Outlined.Person, "View profile") { showMenu = false; onOpenProfile() }
                    MenuRow(Icons.Outlined.Search, "Search") { showMenu = false; onOpenSearch() }
                    MenuRow(Icons.Outlined.PhotoLibrary, "Media, files and links") { showMenu = false; onOpenSharedMedia() }
                    MenuRow(
                        icon = if (convo.muted) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                        label = if (convo.muted) "Unmute notifications" else "Mute notifications",
                    ) {
                        convo.muted = !convo.muted
                        showMenu = false
                        Toast.makeText(context, if (convo.muted) "Notifications muted" else "Notifications on", Toast.LENGTH_SHORT).show()
                    }
                    MenuRow(Icons.Outlined.Palette, "Wallpaper") { showMenu = false; onOpenWallpaper() }
                    MenuRow(Icons.Outlined.Delete, "Clear chat") { showMenu = false; showClear = true }
                    MenuRow(Icons.Outlined.Block, "Block", PayraColors.Danger) { showMenu = false; showBlock = true }
                }
            }
        }

        // Attachment sheet
        BottomSheetOverlay(visible = showAttach, onDismiss = { showAttach = false }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SheetOption(Icons.Outlined.Image, "Gallery", PayraColors.Primary, {
                    showAttach = false
                    onOpenPicker()
                })
                SheetOption(Icons.Outlined.CameraAlt, "Camera", Color(0xFF7B6FD6), {
                    showAttach = false
                    launchCamera(1)
                })
                SheetOption(Icons.Outlined.Videocam, "Video", Color(0xFFF28B66), {
                    showAttach = false
                    launchCamera(2)
                })
            }
            Spacer(Modifier.size(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SheetOption(Icons.Outlined.InsertDriveFile, "Document", Color(0xFF3FA796), {
                    showAttach = false
                    pickDocument.launch(arrayOf("*/*"))
                })
                SheetOption(Icons.Outlined.Contacts, "Contact", Color(0xFFE4A93A), {
                    showAttach = false
                    onOpenContactPicker()
                })
                SheetOption(Icons.Outlined.LocationOn, "Location", Color(0xFF2B8FAE), {
                    showAttach = false
                    onOpenShareLocation()
                })
            }
        }

        // Delete sheet
        BottomSheetOverlay(visible = showDelete, onDismiss = { showDelete = false }) {
            Text("Delete message?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Choose how to delete this message.", color = PayraColors.Muted, fontSize = 14.sp)
            Spacer(Modifier.size(14.dp))
            if (canDeleteForEveryone) {
                SheetButton("Delete for everyone", PayraColors.Danger, onClick = {
                    selectedMessages.forEach { FakeChatStore.deleteForEveryone(convo, it.id) }
                    selected.clear()
                    showDelete = false
                })
                Spacer(Modifier.size(10.dp))
            }
            SheetButton("Delete for me", PayraColors.Ink, onClick = {
                selectedMessages.forEach { FakeChatStore.deleteForMe(convo, it.id) }
                selected.clear()
                showDelete = false
            })
            Spacer(Modifier.size(10.dp))
            SheetButton("Cancel", PayraColors.Primary, onClick = { showDelete = false })
        }

        // Clear chat sheet
        BottomSheetOverlay(visible = showClear, onDismiss = { showClear = false }) {
            Text("Clear this chat?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("All messages in this chat will be removed from your phone.", color = PayraColors.Muted, fontSize = 14.sp)
            Spacer(Modifier.size(14.dp))
            SheetButton("Clear chat", PayraColors.Danger, onClick = {
                FakeChatStore.clearChat(convo)
                showClear = false
            })
            Spacer(Modifier.size(10.dp))
            SheetButton("Cancel", PayraColors.Primary, onClick = { showClear = false })
        }

        // Block sheet
        BottomSheetOverlay(visible = showBlock, onDismiss = { showBlock = false }) {
            Text("Block ${convo.contact.name}?", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("They will not be able to message or call you.", color = PayraColors.Muted, fontSize = 14.sp)
            Spacer(Modifier.size(14.dp))
            SheetButton("Block", PayraColors.Danger, onClick = {
                FakeChatStore.block(convo.contact.id)
                showBlock = false
                onBack()
            })
            Spacer(Modifier.size(10.dp))
            SheetButton("Cancel", PayraColors.Primary, onClick = { showBlock = false })
        }

        // Full emoji list for reactions
        BottomSheetOverlay(visible = showReactionSheet, onDismiss = { showReactionSheet = false }) {
            Text("React with", color = PayraColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(8.dp))
            EmojiPanel(
                onPick = { emoji ->
                    selected.firstOrNull()?.let { FakeChatStore.toggleReaction(convo, it, emoji) }
                    selected.clear()
                    showReactionSheet = false
                },
                height = 330.dp,
            )
        }
    }
}

@Composable
private fun LazyItemScope.CallLogRow(message: ChatMessage, onCallBack: () -> Unit) {
    Box(modifier = Modifier.animateItem().fillMaxWidth()) {
        CallLogChip(message = message, onCallBack = onCallBack)
    }
}

/**
 * Header: back, profile pill, call + menu pill. The first tap on the call icon makes the pill grow with a spring
 * and show the video-call icon next to it while the profile pill shrinks. The next tap starts the call.
 */
@Composable
private fun ChatHeader(
    contact: Contact,
    typing: Boolean,
    muted: Boolean,
    callExpanded: Boolean,
    onBack: () -> Unit,
    onCallTap: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onProfileTap: () -> Unit,
    onMenu: () -> Unit,
) {
    val capsuleColor by animateColorAsState(
        targetValue = if (callExpanded) PayraColors.Primary.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(240),
        label = "callCapsule",
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
        Spacer(Modifier.width(8.dp))
        GlassSurface(modifier = Modifier.weight(1f), shape = PayraShapes.Pill, onClick = onProfileTap) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarCircle(initial = contact.name.take(1), color = contact.color, size = 40.dp, imageUri = contact.avatarUri)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            contact.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = PayraColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        AnimatedVisibility(
                            visible = muted,
                            enter = scaleIn(spring(dampingRatio = 0.5f, stiffness = 500f), initialScale = 0.3f) + fadeIn(),
                            exit = scaleOut(targetScale = 0.3f) + fadeOut(),
                        ) {
                            Icon(
                                Icons.Outlined.NotificationsOff,
                                contentDescription = "Muted",
                                tint = PayraColors.Muted,
                                modifier = Modifier.padding(start = 6.dp).size(16.dp),
                            )
                        }
                    }
                    AnimatedContent(
                        targetState = typing,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "status",
                    ) { isTyping ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isTyping) {
                                OnlineDot()
                                Spacer(Modifier.width(5.dp))
                            }
                            Text(
                                text = if (isTyping) "typing..." else "online",
                                fontSize = 12.sp,
                                color = PayraColors.Primary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        GlassSurface(
            modifier = Modifier.animateContentSize(spring(dampingRatio = 0.72f, stiffness = 360f)),
            shape = PayraShapes.Pill,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(capsuleColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Call,
                        contentDescription = "Voice call",
                        tint = if (callExpanded) PayraColors.Primary else PayraColors.Ink,
                        modifier = Modifier
                            .size(22.dp)
                            .bouncyClick(onClick = if (callExpanded) onAudioCall else onCallTap, pressedScale = 0.78f),
                    )
                    AnimatedVisibility(
                        visible = callExpanded,
                        enter = fadeIn(tween(220)) + scaleIn(spring(dampingRatio = 0.55f, stiffness = 450f), initialScale = 0.3f) + expandHorizontally(),
                        exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.3f) + shrinkHorizontally(),
                    ) {
                        Icon(
                            Icons.Outlined.Videocam,
                            contentDescription = "Video call",
                            tint = PayraColors.Primary,
                            modifier = Modifier.size(22.dp).bouncyClick(onClick = onVideoCall, pressedScale = 0.78f),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "More",
                    tint = PayraColors.Ink,
                    modifier = Modifier.size(22.dp).bouncyClick(onClick = onMenu, pressedScale = 0.78f),
                )
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun OnlineDot() {
    val transition = rememberInfiniteTransition(label = "online")
    val pulse = transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            }
            .clip(CircleShape)
            .background(PayraColors.Success),
    )
}

@Composable
private fun DateChip(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        GlassSurface(shape = PayraShapes.Pill, fillAlpha = 0.5f) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PayraColors.Muted,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

/** One message row: selection check, swipe-to-reply, double-tap heart, bubble, reaction chips. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.MessageRow(
    message: ChatMessage,
    animateIn: Boolean,
    selecting: Boolean,
    selected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onSwipeReply: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    onReact: (String) -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    onRetry: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val swipe = remember { Animatable(0f) }
    val heart = remember { Animatable(1f) }
    val thresholdPx = with(LocalDensity.current) { 64.dp.toPx() }
    val maxPx = with(LocalDensity.current) { 88.dp.toPx() }
    val highlight by animateColorAsState(
        targetValue = if (selected) PayraColors.Primary.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(180),
        label = "rowHighlight",
    )
    val sent = message.sent
    val heartEmoji = "\u2764\uFE0F"

    Row(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(highlight)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = selecting,
            enter = fadeIn(tween(160)) + expandHorizontally(spring(dampingRatio = 0.7f, stiffness = 450f)),
            exit = fadeOut(tween(120)) + shrinkHorizontally(),
        ) {
            val checkFill by animateColorAsState(
                targetValue = if (selected) PayraColors.Primary else Color.Transparent,
                animationSpec = tween(160),
                label = "checkFill",
            )
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, end = 10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(checkFill)
                    .border(2.dp, if (selected) PayraColors.Primary else PayraColors.Muted.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = null,
                tint = PayraColors.Primary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .graphicsLayer {
                        val p = (swipe.value / thresholdPx).coerceIn(0f, 1f)
                        alpha = p
                        scaleX = 0.5f + 0.5f * p
                        scaleY = 0.5f + 0.5f * p
                    },
            )
            Column(
                modifier = Modifier
                    .align(if (sent) Alignment.CenterEnd else Alignment.CenterStart)
                    .offset { IntOffset(swipe.value.roundToInt(), 0) }
                    .pointerInput(selecting) {
                        if (!selecting) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (swipe.value >= thresholdPx) onSwipeReply()
                                        swipe.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 500f))
                                    }
                                },
                                onDragCancel = {
                                    scope.launch { swipe.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 500f)) }
                                },
                            ) { change, dragAmount ->
                                change.consume()
                                scope.launch { swipe.snapTo((swipe.value + dragAmount).coerceIn(0f, maxPx)) }
                            }
                        }
                    },
                horizontalAlignment = if (sent) Alignment.End else Alignment.Start,
            ) {
                val bubbleModifier = Modifier
                    .popIn(enabled = animateIn, fromEnd = sent)
                    .widthIn(max = 290.dp)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onLongClick = onLongClick,
                        onDoubleClick = if (selecting || message.deleted) {
                            null
                        } else {
                            {
                                onReact(heartEmoji)
                                scope.launch {
                                    heart.snapTo(0f)
                                    heart.animateTo(1f, tween(700))
                                }
                                Unit
                            }
                        },
                        onClick = onClick,
                    )
                Box {
                    if (sent) {
                        Box(
                            modifier = bubbleModifier
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 6.dp, bottomStart = 22.dp))
                                .background(PayraColors.Primary)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            BubbleBody(message = message, onQuoteClick = onQuoteClick, onRetry = onRetry, onOpenMedia = onOpenMedia)
                        }
                    } else {
                        GlassSurface(
                            modifier = bubbleModifier,
                            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 6.dp),
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                BubbleBody(message = message, onQuoteClick = onQuoteClick, onRetry = onRetry, onOpenMedia = onOpenMedia)
                            }
                        }
                    }
                    // Big heart that pops out of the bubble on double-tap
                    Text(
                        text = heartEmoji,
                        fontSize = 56.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                val p = heart.value
                                alpha = (1f - p).coerceIn(0f, 1f)
                                val s = 0.4f + 1.1f * p
                                scaleX = s
                                scaleY = s
                                translationY = -p * 60.dp.toPx()
                            },
                    )
                }
                AnimatedVisibility(
                    visible = message.reactions.isNotEmpty(),
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f), initialScale = 0.4f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.4f) + fadeOut(),
                ) {
                    ReactionChips(
                        reactions = message.reactions,
                        onTap = onReact,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp).offset(y = (-10).dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.TypingBubble() {
    Row(modifier = Modifier.animateItem().fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        GlassSurface(
            modifier = Modifier.popIn(enabled = true, fromEnd = false),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 6.dp),
        ) {
            BouncingDots(
                color = PayraColors.Muted,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            )
        }
    }
}
