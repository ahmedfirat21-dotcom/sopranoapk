package com.soprano.chat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Discover : Screen(
        route = "discover",
        title = "Keşfet",
        icon = Icons.Outlined.Explore
    )
    data object Lobby : Screen(
        route = "lobby",
        title = "Lobi",
        icon = Icons.Outlined.Layers
    )
    data object Connections : Screen(
        route = "connections",
        title = "Bağlantılar",
        icon = Icons.Outlined.Share
    )
    data object Aura : Screen(
        route = "aura",
        title = "Aura",
        icon = Icons.Outlined.AccountCircle
    )
    data object VipLounge : Screen(
        route = "vip_lounge",
        title = "VIP Lounge"
    )
    data object SplashLogin : Screen(
        route = "splash_login",
        title = "Giriş"
    )
    data object Whisper : Screen(
        route = "whisper",
        title = "Fısıltı"
    )
    data object EchoChain : Screen(
        route = "echo_chain",
        title = "Yankı Zinciri"
    )
    data object Register : Screen(
        route = "register",
        title = "Kayıt"
    )
    data object Studio : Screen(
        route = "studio",
        title = "Stüdyo"
    )
    data object IncomingCall : Screen(
        route = "incoming_call",
        title = "Gelen Frekans"
    )
    data object Search : Screen(
        route = "search",
        title = "Ara"
    )
}

val bottomNavItems = listOf(
    Screen.Discover,
    Screen.Lobby,
    Screen.Connections,
    Screen.Aura
)

// Routes where the floating bottom nav should be hidden
val hideBottomNavRoutes = setOf(
    Screen.VipLounge.route,
    Screen.Whisper.route,
    Screen.EchoChain.route,
    Screen.SplashLogin.route,
    Screen.Register.route,
    Screen.Studio.route,
    Screen.IncomingCall.route,
    Screen.Search.route
)
