package com.payra.calling.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payra.calling.designsystem.GlassScaffold
import com.payra.calling.designsystem.GlassTabBar
import com.payra.calling.designsystem.TabItem
import com.payra.calling.feature.calls.CallListScreen
import com.payra.calling.feature.chats.ChatListScreen
import com.payra.calling.feature.contacts.ContactsScreen
import com.payra.calling.feature.settings.SettingsScreen

private val Tabs = listOf(
    TabItem("Chats", Icons.Outlined.ChatBubbleOutline),
    TabItem("Calls", Icons.Outlined.Call),
    TabItem("Contacts", Icons.Outlined.People),
    TabItem("Settings", Icons.Outlined.Tune),
)

/** Main screen after login: tab content runs under the glass tab bar; switching tabs slides + fades. */
@Composable
fun HomeShell(
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onCall: (contactId: String, incoming: Boolean, video: Boolean) -> Unit,
    onViewProfile: (contactId: String) -> Unit,
    onMyProfile: () -> Unit,
    onOpenSetting: (String) -> Unit,
    onLogout: () -> Unit,
    onNewContact: () -> Unit,
    onOpenArchived: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }

    GlassScaffold(
        bottomBar = {
            GlassTabBar(
                items = Tabs,
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val forward = targetState > initialState
                (fadeIn(tween(260)) + slideInHorizontally(tween(340, easing = FastOutSlowInEasing)) { w -> if (forward) w / 8 else -w / 8 }) togetherWith
                    (fadeOut(tween(160)) + slideOutHorizontally(tween(340, easing = FastOutSlowInEasing)) { w -> if (forward) -w / 8 else w / 8 })
            },
            label = "tabs",
        ) { current ->
            when (current) {
                0 -> ChatListScreen(contentPadding = padding, onOpenChat = onOpenChat, onNewChat = onNewChat, onOpenArchived = onOpenArchived)
                1 -> CallListScreen(contentPadding = padding, onCall = onCall)
                2 -> ContactsScreen(contentPadding = padding, onOpenChat = onOpenChat, onViewProfile = onViewProfile, onNewContact = onNewContact)
                else -> SettingsScreen(contentPadding = padding, onMyProfile = onMyProfile, onOpen = onOpenSetting, onLogout = onLogout)
            }
        }
    }
}
