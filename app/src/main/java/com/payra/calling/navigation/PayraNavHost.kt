package com.payra.calling.navigation

import com.payra.calling.feature.settings.LegalKind
import com.payra.calling.feature.settings.LegalScreen
import com.payra.calling.feature.settings.ReportProblemScreen
import com.payra.calling.feature.settings.FaqScreen
import com.payra.calling.feature.share.ShareTargetScreen
import com.payra.calling.data.ShareInbox
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.payra.calling.feature.settings.ChangePhoneScreen
import com.payra.calling.feature.media.MediaPreviewScreen
import com.payra.calling.designsystem.ConnectionBanner
import com.payra.calling.data.NetworkMonitor
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.designsystem.Motion
import com.payra.calling.feature.auth.ForgotPasswordScreen
import com.payra.calling.feature.auth.LoginScreen
import com.payra.calling.feature.auth.ProfileSetupScreen
import com.payra.calling.feature.auth.RegisterScreen
import com.payra.calling.feature.calls.CallManager
import com.payra.calling.feature.calls.CallOverlay
import com.payra.calling.feature.calls.CallScreen
import com.payra.calling.feature.chats.ArchivedChatsScreen
import com.payra.calling.feature.chats.ChatScreen
import com.payra.calling.feature.chats.ChatSearchScreen
import com.payra.calling.feature.chats.ForwardScreen
import com.payra.calling.feature.contacts.AddContactScreen
import com.payra.calling.feature.contacts.ContactPickerScreen
import com.payra.calling.feature.home.HomeShell
import com.payra.calling.feature.location.ShareLocationScreen
import com.payra.calling.feature.media.MediaPickerScreen
import com.payra.calling.feature.media.MediaViewerScreen
import com.payra.calling.feature.media.SharedMediaScreen
import com.payra.calling.feature.newchat.NewChatScreen
import com.payra.calling.feature.permissions.PermissionPages
import com.payra.calling.feature.permissions.PermissionScreen
import com.payra.calling.feature.profile.CropScreen
import com.payra.calling.feature.profile.EditProfileScreen
import com.payra.calling.feature.profile.ProfileScreen
import com.payra.calling.feature.settings.AboutSettingsScreen
import com.payra.calling.feature.settings.AccountSettingsScreen
import com.payra.calling.feature.settings.AppearanceSettingsScreen
import com.payra.calling.feature.settings.BlockedContactsScreen
import com.payra.calling.feature.settings.ChangePasswordScreen
import com.payra.calling.feature.settings.ChatsSettingsScreen
import com.payra.calling.feature.settings.HelpSettingsScreen
import com.payra.calling.feature.settings.LanguageSettingsScreen
import com.payra.calling.feature.settings.LinkGmailScreen
import com.payra.calling.feature.settings.NotificationsSettingsScreen
import com.payra.calling.feature.settings.PrivacySettingsScreen
import com.payra.calling.feature.settings.SettingsRoutes
import com.payra.calling.feature.settings.StorageSettingsScreen
import com.payra.calling.feature.splash.SplashScreen
import com.payra.calling.feature.wallpaper.WallpaperScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE_SETUP = "profile_setup"
    const val PERMISSION_CONTACTS = "permission_contacts"
    const val PERMISSION_MEDIA = "permission_media"
    const val PERMISSION_NOTIFICATIONS = "permission_notifications"
    const val HOME = "home"
    const val NEW_CHAT = "new_chat"
    const val CHAT = "chat/{contactId}"
    const val FORWARD = "forward/{contactId}/{ids}"
    const val CALL = "call"
    const val PROFILE = "profile/{contactId}/{self}"
    const val EDIT_PROFILE = "edit_profile"
    const val MEDIA_VIEWER = "media_viewer?uri={uri}&video={video}&dur={dur}&tone={tone}&title={title}"
    const val CROP = "crop/{kind}?uri={uri}"
    const val MEDIA_PREVIEW = "media_preview/{contactId}?items={items}"
    const val SHARE_TARGET = "share_target"
    const val PICKER = "picker/{contactId}"
    const val CONTACT_PICKER = "contact_picker/{contactId}"
    const val SHARE_LOCATION = "share_location/{contactId}"
    const val SETTING = "setting/{name}"
    const val CHAT_SEARCH = "chat_search/{contactId}"
    const val SHARED_MEDIA = "shared_media/{contactId}"
    const val WALLPAPER = "wallpaper"
    const val WALLPAPER_CHAT = "wallpaper_chat/{contactId}"
    const val ADD_CONTACT = "add_contact"
    const val ARCHIVED = "archived"

    fun chat(contactId: String) = "chat/$contactId"
    fun forward(contactId: String, ids: List<Long>) = "forward/$contactId/${ids.joinToString(",")}"
    fun profile(contactId: String, self: Boolean) = "profile/$contactId/$self"
    fun mediaViewer(item: MediaItem, title: String) =
        "media_viewer?uri=${Uri.encode(item.uri)}&video=${item.isVideo}&dur=${Uri.encode(item.duration)}&tone=${item.tone}&title=${Uri.encode(title)}"
    fun crop(kind: String, uri: String) = "crop/$kind?uri=${Uri.encode(uri)}"
    fun mediaPreview(contactId: String, items: List<MediaItem>) =
        "media_preview/$contactId?items=" + Uri.encode(
            items.joinToString(",") { Uri.encode("${it.isVideo}~${Uri.encode(it.duration)}~${Uri.encode(it.uri)}") },
        )
    fun picker(contactId: String) = "picker/$contactId"
    fun contactPicker(contactId: String) = "contact_picker/$contactId"
    fun shareLocation(contactId: String) = "share_location/$contactId"
    fun setting(name: String) = "setting/$name"
    fun chatSearch(contactId: String) = "chat_search/$contactId"
    fun sharedMedia(contactId: String) = "shared_media/$contactId"
    fun wallpaperChat(contactId: String) = "wallpaper_chat/$contactId"
}

/** Navigation normally hands back readable text; if an address still looks percent-encoded, decode it once. */
private fun decodeIfNeeded(value: String): String =
    if (value.startsWith("content%3A") || value.startsWith("file%3A") || value.startsWith("http%3A") || value.startsWith("https%3A") || value.contains("%2F") || value.contains("%20")) Uri.decode(value) else value

/** Turns the "items" argument of the preview screen back into pictures / videos. */
private fun decodePreviewItems(raw: String): List<MediaItem> {
    val joined = if (raw.contains("%2C") && !raw.contains(",")) Uri.decode(raw) else raw
    return joined.split(",").filter { it.isNotBlank() }.mapNotNull { token ->
        val parts = Uri.decode(token).split("~", limit = 3)
        if (parts.size < 3) null else MediaItem(0, parts[0] == "true", Uri.decode(parts[1]), Uri.decode(parts[2]))
    }
}

@Composable
fun PayraNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) { NetworkMonitor.start(context.applicationContext) }
    val slide = tween<IntOffset>(Motion.ScreenMillis, easing = FastOutSlowInEasing)

    // Clears the whole back stack, so "Back" never returns to login after signing in.
    val goHome: () -> Unit = {
        nav.navigate(Routes.HOME) {
            popUpTo(nav.graph.id) { inclusive = true }
        }
    }
    val goLogin: () -> Unit = {
        nav.navigate(Routes.LOGIN) {
            popUpTo(nav.graph.id) { inclusive = true }
        }
    }

    fun startCall(contactId: String, incoming: Boolean, video: Boolean) {
        if (FakeChatStore.isBlocked(contactId)) {
            Toast.makeText(context, "Unblock this person first", Toast.LENGTH_SHORT).show()
            return
        }
        // start() refuses when another call is still running; never open the old call for a different person.
        if (!CallManager.start(context, contactId, incoming, video)) {
            Toast.makeText(context, "Finish your current call first", Toast.LENGTH_SHORT).show()
            return
        }
        CallManager.minimized = false
        nav.navigate(Routes.CALL) { launchSingleTop = true }
    }

    fun openViewer(item: MediaItem, title: String) {
        // Everything the viewer needs travels inside the route, so it also works after the app was restarted.
        nav.navigate(Routes.mediaViewer(item, title))
    }

    fun sendAndFinish(contactId: String, send: () -> Long) {
        val convo = FakeChatStore.conversationFor(contactId)
        val id = send()
        FakeChatStore.simulateDelivery(convo, id, 2200L)
        nav.popBackStack()
    }

    // Something was shared to Payra from another app: let the person pick a chat (only after login)
    val backEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route
    val sharePending = ShareInbox.pending
    LaunchedEffect(sharePending, currentRoute) {
        val authRoutes = setOf(
            Routes.SPLASH, Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD, Routes.PROFILE_SETUP,
            Routes.PERMISSION_CONTACTS, Routes.PERMISSION_MEDIA, Routes.PERMISSION_NOTIFICATIONS,
        )
        if (sharePending != null && currentRoute != null && currentRoute !in authRoutes && currentRoute != Routes.SHARE_TARGET) {
            nav.navigate(Routes.SHARE_TARGET) { launchSingleTop = true }
        }
    }

    // The app is leaving the screen during a call: show the call screen so the small window shows the call.
    val pipRequested = CallManager.pipRequested
    LaunchedEffect(pipRequested) {
        if (pipRequested && CallManager.active) {
            CallManager.minimized = false
            if (nav.currentDestination?.route != Routes.CALL) nav.navigate(Routes.CALL) { launchSingleTop = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            enterTransition = { slideInHorizontally(animationSpec = slide) { w -> w / 4 } + fadeIn(tween(320)) },
            exitTransition = { slideOutHorizontally(animationSpec = slide) { w -> -w / 6 } + fadeOut(tween(220)) },
            popEnterTransition = { slideInHorizontally(animationSpec = slide) { w -> -w / 6 } + fadeIn(tween(320)) },
            popExitTransition = { slideOutHorizontally(animationSpec = slide) { w -> w / 4 } + fadeOut(tween(220)) },
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } })
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = goHome,
                    onForgotPassword = { nav.navigate(Routes.FORGOT_PASSWORD) },
                    onCreateAccount = { nav.navigate(Routes.REGISTER) },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(onRegistered = { nav.navigate(Routes.PROFILE_SETUP) }, onLogin = { nav.popBackStack() })
            }
            composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.PROFILE_SETUP) {
                ProfileSetupScreen(onFinish = { nav.navigate(Routes.PERMISSION_CONTACTS) })
            }
            composable(Routes.PERMISSION_CONTACTS) {
                PermissionScreen(
                    page = PermissionPages.Contacts,
                    onAllow = { nav.navigate(Routes.PERMISSION_MEDIA) },
                    onSkip = { nav.navigate(Routes.PERMISSION_MEDIA) },
                )
            }
            composable(Routes.PERMISSION_MEDIA) {
                PermissionScreen(
                    page = PermissionPages.MicAndCamera,
                    onAllow = { nav.navigate(Routes.PERMISSION_NOTIFICATIONS) },
                    onSkip = { nav.navigate(Routes.PERMISSION_NOTIFICATIONS) },
                )
            }
            composable(Routes.PERMISSION_NOTIFICATIONS) {
                PermissionScreen(page = PermissionPages.Notifications, onAllow = goHome, onSkip = goHome)
            }
            composable(Routes.HOME) {
                HomeShell(
                    onOpenChat = { contactId -> nav.navigate(Routes.chat(contactId)) },
                    onNewChat = { nav.navigate(Routes.NEW_CHAT) },
                    onCall = { contactId, incoming, video -> startCall(contactId, incoming, video) },
                    onViewProfile = { contactId -> nav.navigate(Routes.profile(contactId, false)) },
                    onMyProfile = { nav.navigate(Routes.profile(FakeChatStore.me.id, true)) },
                    onOpenSetting = { name -> nav.navigate(Routes.setting(name)) },
                    onLogout = goLogin,
                    onNewContact = { nav.navigate(Routes.ADD_CONTACT) },
                    onOpenArchived = { nav.navigate(Routes.ARCHIVED) },
                )
            }
            composable(Routes.NEW_CHAT) {
                NewChatScreen(
                    onBack = { nav.popBackStack() },
                    onOpenChat = { contactId ->
                        nav.navigate(Routes.chat(contactId)) { popUpTo(Routes.NEW_CHAT) { inclusive = true } }
                    },
                    onNewContact = { nav.navigate(Routes.ADD_CONTACT) },
                )
            }
            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                ChatScreen(
                    contactId = contactId,
                    onBack = { nav.popBackStack() },
                    onForward = { ids -> nav.navigate(Routes.forward(contactId, ids)) },
                    onCall = { video -> startCall(contactId, false, video) },
                    onOpenProfile = { nav.navigate(Routes.profile(contactId, false)) },
                    onOpenMedia = { item -> openViewer(item, "") },
                    onOpenPicker = { nav.navigate(Routes.picker(contactId)) },
                    onOpenContactPicker = { nav.navigate(Routes.contactPicker(contactId)) },
                    onOpenShareLocation = { nav.navigate(Routes.shareLocation(contactId)) },
                    onOpenSearch = { nav.navigate(Routes.chatSearch(contactId)) },
                    onOpenSharedMedia = { nav.navigate(Routes.sharedMedia(contactId)) },
                    onOpenWallpaper = { nav.navigate(Routes.wallpaperChat(contactId)) },
                    onPreviewMedia = { items -> nav.navigate(Routes.mediaPreview(contactId, items)) },
                )
            }
            composable(
                route = Routes.CHAT_SEARCH,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                ChatSearchScreen(contactId = entry.arguments?.getString("contactId").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(
                route = Routes.SHARED_MEDIA,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                SharedMediaScreen(
                    contactId = entry.arguments?.getString("contactId").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onOpenMedia = { item -> openViewer(item, "") },
                )
            }
            composable(Routes.WALLPAPER) { WallpaperScreen(contactId = null, onBack = { nav.popBackStack() }) }
            composable(
                route = Routes.WALLPAPER_CHAT,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                WallpaperScreen(contactId = entry.arguments?.getString("contactId").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.ADD_CONTACT) {
                AddContactScreen(onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
            }
            composable(Routes.ARCHIVED) {
                ArchivedChatsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenChat = { id -> nav.navigate(Routes.chat(id)) },
                )
            }
            composable(
                route = Routes.FORWARD,
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType },
                    navArgument("ids") { type = NavType.StringType },
                ),
            ) { entry ->
                val fromId = entry.arguments?.getString("contactId").orEmpty()
                val ids = entry.arguments?.getString("ids").orEmpty().split(",").mapNotNull { it.toLongOrNull() }
                ForwardScreen(
                    fromContactId = fromId,
                    messageIds = ids,
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() },
                )
            }
            composable(Routes.CALL) {
                CallScreen(
                    onMinimize = { nav.popBackStack() },
                    onClose = { nav.popBackStack() },
                    onMessage = { id ->
                        nav.popBackStack()
                        nav.navigate(Routes.chat(id))
                    },
                )
            }
            composable(
                route = Routes.PROFILE,
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType },
                    navArgument("self") { type = NavType.StringType },
                ),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                val self = entry.arguments?.getString("self") == "true"
                ProfileScreen(
                    contactId = contactId,
                    isSelf = self,
                    onBack = { nav.popBackStack() },
                    onOpenChat = { id -> nav.navigate(Routes.chat(id)) },
                    onCall = { id, video -> startCall(id, false, video) },
                    onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                    onOpenSharedMedia = { nav.navigate(Routes.sharedMedia(contactId)) },
                    onViewImage = { item, title -> openViewer(item, title) },
                )
            }
            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(
                    contactId = FakeChatStore.me.id,
                    onBack = { nav.popBackStack() },
                    onCrop = { kind, uri -> nav.navigate(Routes.crop(kind, uri)) },
                )
            }
            composable(
                route = Routes.CROP,
                arguments = listOf(
                    navArgument("kind") { type = NavType.StringType },
                    navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                val kind = entry.arguments?.getString("kind").orEmpty()
                val uri = decodeIfNeeded(entry.arguments?.getString("uri").orEmpty())
                CropScreen(
                    kind = kind,
                    sourceUri = uri,
                    onCancel = { nav.popBackStack() },
                    onDone = { saved ->
                        FakeChatStore.updateContact(FakeChatStore.me.id) {
                            if (kind == "cover") it.copy(coverUri = saved) else it.copy(avatarUri = saved)
                        }
                        nav.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.MEDIA_VIEWER,
                arguments = listOf(
                    navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                    navArgument("video") { type = NavType.StringType; defaultValue = "false" },
                    navArgument("dur") { type = NavType.StringType; defaultValue = "" },
                    navArgument("tone") { type = NavType.StringType; defaultValue = "0" },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                val args = entry.arguments
                MediaViewerScreen(
                    uri = decodeIfNeeded(args?.getString("uri").orEmpty()),
                    isVideo = args?.getString("video") == "true",
                    duration = decodeIfNeeded(args?.getString("dur").orEmpty()),
                    tone = args?.getString("tone")?.toIntOrNull() ?: 0,
                    title = decodeIfNeeded(args?.getString("title").orEmpty()),
                    onBack = { nav.popBackStack() },
                )
            }
            composable(
                route = Routes.PICKER,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                MediaPickerScreen(
                    onBack = { nav.popBackStack() },
                    onSend = { media -> nav.navigate(Routes.mediaPreview(contactId, media)) },
                )
            }
            composable(
                route = Routes.MEDIA_PREVIEW,
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType },
                    navArgument("items") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                val initial = remember(entry) { decodePreviewItems(entry.arguments?.getString("items").orEmpty()) }
                MediaPreviewScreen(
                    initial = initial,
                    onBack = { nav.popBackStack() },
                    onSend = { items, caption ->
                        val convo = FakeChatStore.conversationFor(contactId)
                        val message = FakeChatStore.sendMedia(convo, items, caption)
                        FakeChatStore.simulateDelivery(convo, message.id, 2200L)
                        // back to the chat (also closes the gallery picker that was opened before)
                        if (!nav.popBackStack(Routes.CHAT, false)) nav.popBackStack()
                    },
                )
            }
            composable(Routes.SHARE_TARGET) {
                val payload = ShareInbox.pending
                if (payload == null) {
                    LaunchedEffect(Unit) { nav.popBackStack() }
                } else {
                    ShareTargetScreen(
                        payload = payload,
                        onBack = {
                            ShareInbox.pending = null
                            nav.popBackStack()
                        },
                        onPick = { id ->
                            val convo = FakeChatStore.conversationFor(id)
                            if (payload.text.isNotBlank()) {
                                val m = FakeChatStore.send(convo, payload.text)
                                FakeChatStore.simulateDelivery(convo, m.id, 1500L)
                            }
                            payload.files.forEach { file ->
                                val m = FakeChatStore.sendFile(convo, file.name, file.info, file.uri)
                                FakeChatStore.simulateDelivery(convo, m.id, 2200L)
                            }
                            val items = payload.items
                            ShareInbox.pending = null
                            nav.popBackStack()
                            nav.navigate(Routes.chat(id))
                            if (items.isNotEmpty()) nav.navigate(Routes.mediaPreview(id, items))
                        },
                    )
                }
            }
            composable(
                route = Routes.CONTACT_PICKER,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                ContactPickerScreen(
                    onBack = { nav.popBackStack() },
                    onPicked = { name, phone ->
                        sendAndFinish(contactId) { FakeChatStore.sendContactCard(FakeChatStore.conversationFor(contactId), name, phone).id }
                    },
                )
            }
            composable(
                route = Routes.SHARE_LOCATION,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                ShareLocationScreen(
                    onBack = { nav.popBackStack() },
                    onSend = { name, address ->
                        sendAndFinish(contactId) { FakeChatStore.sendLocation(FakeChatStore.conversationFor(contactId), name, address).id }
                    },
                )
            }
            composable(
                route = Routes.SETTING,
                arguments = listOf(navArgument("name") { type = NavType.StringType }),
            ) { entry ->
                val back: () -> Unit = { nav.popBackStack() }
                when (entry.arguments?.getString("name")) {
                    SettingsRoutes.ACCOUNT -> AccountSettingsScreen(
                        onBack = back,
                        onOpenPassword = { nav.navigate(Routes.setting(SettingsRoutes.PASSWORD)) },
                        onOpenGmail = { nav.navigate(Routes.setting(SettingsRoutes.GMAIL)) },
                        onOpenPhone = { nav.navigate(Routes.setting(SettingsRoutes.PHONE)) },
                        onDeleted = goLogin,
                    )
                    SettingsRoutes.PASSWORD -> ChangePasswordScreen(back)
                    SettingsRoutes.PHONE -> ChangePhoneScreen(back)
                    SettingsRoutes.GMAIL -> LinkGmailScreen(back)
                    SettingsRoutes.PRIVACY -> PrivacySettingsScreen(back, onOpenBlocked = { nav.navigate(Routes.setting(SettingsRoutes.BLOCKED)) })
                    SettingsRoutes.BLOCKED -> BlockedContactsScreen(back)
                    SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(back)
                    SettingsRoutes.CHATS -> ChatsSettingsScreen(back, onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) })
                    SettingsRoutes.STORAGE -> StorageSettingsScreen(back)
                    SettingsRoutes.APPEARANCE -> AppearanceSettingsScreen(back, onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) })
                    SettingsRoutes.LANGUAGE -> LanguageSettingsScreen(back)
                    SettingsRoutes.ABOUT -> AboutSettingsScreen(back)
                    SettingsRoutes.FAQ -> FaqScreen(back)
                    SettingsRoutes.REPORT -> ReportProblemScreen(back)
                    SettingsRoutes.TERMS -> LegalScreen(LegalKind.Terms, back)
                    SettingsRoutes.POLICY -> LegalScreen(LegalKind.Privacy, back)
                    else -> HelpSettingsScreen(
                        back,
                        onOpenFaq = { nav.navigate(Routes.setting(SettingsRoutes.FAQ)) },
                        onOpenReport = { nav.navigate(Routes.setting(SettingsRoutes.REPORT)) },
                        onOpenTerms = { nav.navigate(Routes.setting(SettingsRoutes.TERMS)) },
                        onOpenPolicy = { nav.navigate(Routes.setting(SettingsRoutes.POLICY)) },
                    )
                }
            }
        }

        // The floating bar / video window while a call is minimized (above every screen)
        CallOverlay(
            onExpand = {
                CallManager.minimized = false
                nav.navigate(Routes.CALL) { launchSingleTop = true }
            },
            modifier = Modifier.align(Alignment.TopStart),
        )

        // No internet / connecting notice
        ConnectionBanner(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 4.dp))
    }
}
