package com.payra.calling.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.payra.calling.data.FakeChatStore
import com.payra.calling.data.MediaItem
import com.payra.calling.data.MessageStatus
import com.payra.calling.designsystem.Motion
import com.payra.calling.feature.auth.ForgotPasswordScreen
import com.payra.calling.feature.auth.LoginScreen
import com.payra.calling.feature.auth.ProfileSetupScreen
import com.payra.calling.feature.auth.RegisterScreen
import com.payra.calling.feature.calls.CallScreen
import com.payra.calling.feature.chats.ArchivedChatsScreen
import com.payra.calling.feature.chats.ChatScreen
import com.payra.calling.feature.chats.ChatSearchScreen
import com.payra.calling.feature.contacts.AddContactScreen
import com.payra.calling.feature.media.SharedMediaScreen
import com.payra.calling.feature.settings.BlockedContactsScreen
import com.payra.calling.feature.wallpaper.WallpaperScreen
import com.payra.calling.feature.chats.ForwardScreen
import com.payra.calling.feature.home.HomeShell
import com.payra.calling.feature.media.CameraScreen
import com.payra.calling.feature.media.MediaPickerScreen
import com.payra.calling.feature.media.MediaViewerScreen
import com.payra.calling.feature.newchat.NewChatScreen
import com.payra.calling.feature.permissions.PermissionPages
import com.payra.calling.feature.permissions.PermissionScreen
import com.payra.calling.feature.profile.EditProfileScreen
import com.payra.calling.feature.profile.ProfileScreen
import com.payra.calling.feature.settings.AccountSettingsScreen
import com.payra.calling.feature.settings.ChatsSettingsScreen
import com.payra.calling.feature.settings.HelpSettingsScreen
import com.payra.calling.feature.settings.LanguageSettingsScreen
import com.payra.calling.feature.settings.NotificationsSettingsScreen
import com.payra.calling.feature.settings.PrivacySettingsScreen
import com.payra.calling.feature.settings.SettingsRoutes
import com.payra.calling.feature.splash.SplashScreen

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
    const val CALL = "call/{contactId}/{incoming}/{video}"
    const val PROFILE = "profile/{contactId}/{self}"
    const val EDIT_PROFILE = "edit_profile"
    const val MEDIA_VIEWER = "media/{tone}/{video}"
    const val PICKER = "picker/{contactId}"
    const val CAMERA = "camera/{contactId}"
    const val SETTING = "setting/{name}"
    const val CHAT_SEARCH = "chat_search/{contactId}"
    const val SHARED_MEDIA = "shared_media/{contactId}"
    const val WALLPAPER = "wallpaper"
    const val ADD_CONTACT = "add_contact"
    const val ARCHIVED = "archived"

    fun chat(contactId: String) = "chat/$contactId"
    fun forward(contactId: String, ids: List<Long>) = "forward/$contactId/${ids.joinToString(",")}"
    fun call(contactId: String, incoming: Boolean, video: Boolean) = "call/$contactId/$incoming/$video"
    fun profile(contactId: String, self: Boolean) = "profile/$contactId/$self"
    fun media(tone: Int, video: Boolean) = "media/$tone/$video"
    fun picker(contactId: String) = "picker/$contactId"
    fun camera(contactId: String) = "camera/$contactId"
    fun setting(name: String) = "setting/$name"
    fun chatSearch(contactId: String) = "chat_search/$contactId"
    fun sharedMedia(contactId: String) = "shared_media/$contactId"
}

private fun sendFakeMedia(contactId: String, media: List<MediaItem>) {
    val convo = FakeChatStore.conversationFor(contactId)
    val message = FakeChatStore.sendMedia(convo, media)
    FakeChatStore.setStatus(convo, message.id, MessageStatus.Read)
}

@Composable
fun PayraNavHost() {
    val nav = rememberNavController()
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
                onCall = { contactId, incoming, video -> nav.navigate(Routes.call(contactId, incoming, video)) },
                onViewProfile = { contactId -> nav.navigate(Routes.profile(contactId, false)) },
                onMyProfile = { nav.navigate(Routes.profile(FakeChatStore.contacts.first().id, true)) },
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
                onCall = { video -> nav.navigate(Routes.call(contactId, false, video)) },
                onOpenProfile = { nav.navigate(Routes.profile(contactId, false)) },
                onOpenMedia = { tone, video -> nav.navigate(Routes.media(tone, video)) },
                onOpenPicker = { nav.navigate(Routes.picker(contactId)) },
                onOpenCamera = { nav.navigate(Routes.camera(contactId)) },
                onOpenSearch = { nav.navigate(Routes.chatSearch(contactId)) },
                onOpenSharedMedia = { nav.navigate(Routes.sharedMedia(contactId)) },
                onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) },
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
                onOpenMedia = { item -> nav.navigate(Routes.media(item.tone, item.isVideo)) },
            )
        }
        composable(Routes.WALLPAPER) { WallpaperScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ADD_CONTACT) {
            AddContactScreen(
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() },
            )
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
        composable(
            route = Routes.CALL,
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("incoming") { type = NavType.StringType },
                navArgument("video") { type = NavType.StringType },
            ),
        ) { entry ->
            val contactId = entry.arguments?.getString("contactId").orEmpty()
            val incoming = entry.arguments?.getString("incoming") == "true"
            val video = entry.arguments?.getString("video") == "true"
            CallScreen(contactId = contactId, incoming = incoming, video = video, onEnd = { nav.popBackStack() })
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
                onCall = { id, video -> nav.navigate(Routes.call(id, false, video)) },
                onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
            )
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(contactId = FakeChatStore.contacts.first().id, onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.MEDIA_VIEWER,
            arguments = listOf(
                navArgument("tone") { type = NavType.StringType },
                navArgument("video") { type = NavType.StringType },
            ),
        ) { entry ->
            val tone = entry.arguments?.getString("tone")?.toIntOrNull() ?: 0
            val video = entry.arguments?.getString("video") == "true"
            MediaViewerScreen(tone = tone, isVideo = video, onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.PICKER,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) { entry ->
            val contactId = entry.arguments?.getString("contactId").orEmpty()
            MediaPickerScreen(
                onBack = { nav.popBackStack() },
                onOpenCamera = { nav.navigate(Routes.camera(contactId)) },
                onSend = { media ->
                    sendFakeMedia(contactId, media)
                    nav.popBackStack()
                },
            )
        }
        composable(
            route = Routes.CAMERA,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) { entry ->
            val contactId = entry.arguments?.getString("contactId").orEmpty()
            CameraScreen(
                onClose = { nav.popBackStack() },
                onCapture = { item ->
                    sendFakeMedia(contactId, listOf(item))
                    nav.popBackStack()
                },
            )
        }
        composable(
            route = Routes.SETTING,
            arguments = listOf(navArgument("name") { type = NavType.StringType }),
        ) { entry ->
            val back: () -> Unit = { nav.popBackStack() }
            when (entry.arguments?.getString("name")) {
                SettingsRoutes.ACCOUNT -> AccountSettingsScreen(back)
                SettingsRoutes.PRIVACY -> PrivacySettingsScreen(back, onOpenBlocked = { nav.navigate(Routes.setting(SettingsRoutes.BLOCKED)) })
                SettingsRoutes.BLOCKED -> BlockedContactsScreen(back)
                SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(back)
                SettingsRoutes.CHATS -> ChatsSettingsScreen(back, onOpenWallpaper = { nav.navigate(Routes.WALLPAPER) })
                SettingsRoutes.LANGUAGE -> LanguageSettingsScreen(back)
                else -> HelpSettingsScreen(back)
            }
        }
    }
}
