package com.payra.calling.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MediaViewerHolder
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
    const val MEDIA_VIEWER = "media_viewer"
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
    fun picker(contactId: String) = "picker/$contactId"
    fun contactPicker(contactId: String) = "contact_picker/$contactId"
    fun shareLocation(contactId: String) = "share_location/$contactId"
    fun setting(name: String) = "setting/$name"
    fun chatSearch(contactId: String) = "chat_search/$contactId"
    fun sharedMedia(contactId: String) = "shared_media/$contactId"
    fun wallpaperChat(contactId: String) = "wallpaper_chat/$contactId"
}

@Composable
fun PayraNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
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
        CallManager.start(context, contactId, incoming, video)
        CallManager.minimized = false
        nav.navigate(Routes.CALL) { launchSingleTop = true }
    }

    fun openViewer(item: MediaItem, title: String) {
        MediaViewerHolder.item = item
        MediaViewerHolder.title = title
        nav.navigate(Routes.MEDIA_VIEWER)
    }

    fun sendAndFinish(contactId: String, send: () -> Long) {
        val convo = FakeChatStore.conversationFor(contactId)
        val id = send()
        FakeChatStore.simulateDelivery(convo, id, 2200L)
        nav.popBackStack()
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
                EditProfileScreen(contactId = FakeChatStore.me.id, onBack = { nav.popBackStack() })
            }
            composable(Routes.MEDIA_VIEWER) {
                MediaViewerScreen(onBack = { nav.popBackStack() })
            }
            composable(
                route = Routes.PICKER,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            ) { entry ->
                val contactId = entry.arguments?.getString("contactId").orEmpty()
                MediaPickerScreen(
                    onBack = { nav.popBackStack() },
                    onSend = { media ->
                        sendAndFinish(contactId) { FakeChatStore.sendMedia(FakeChatStore.conversationFor(contactId), media).id }
                    },
                )
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
                        onDeleted = goLogin,
                    )
                    SettingsRoutes.PASSWORD -> ChangePasswordScreen(back)
                    SettingsRoutes.GMAIL -> LinkGmailScreen(back)
                    SettingsRoutes.PRIVACY -> PrivacySettingsScreen(back, onOpenBlocked = { nav.navigate(Routes.setting(SettingsRoutes.BLOCKED)) })
                    SettingsRoutes.BLOCKED -> BlockedContactsScreen(back)
                    SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(back)
                    SettingsRoutes.CHATS -> ChatsSettingsScreen(back, onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) })
                    SettingsRoutes.STORAGE -> StorageSettingsScreen(back)
                    SettingsRoutes.APPEARANCE -> AppearanceSettingsScreen(back, onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) })
                    SettingsRoutes.LANGUAGE -> LanguageSettingsScreen(back)
                    SettingsRoutes.ABOUT -> AboutSettingsScreen(back)
                    else -> HelpSettingsScreen(back)
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
    }
}
