package com.soprano.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soprano.chat.ui.navigation.FloatingBottomBar
import com.soprano.chat.ui.navigation.Screen
import com.soprano.chat.ui.navigation.hideBottomNavRoutes
import com.soprano.chat.ui.screens.*
import com.soprano.chat.ui.theme.Anthracite
import com.soprano.chat.ui.theme.SopranoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SopranoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom nav in immersive screens
    val showBottomNav = currentRoute !in hideBottomNavRoutes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Anthracite)
    ) {
        // Content
        NavHost(
            navController = navController,
            startDestination = Screen.SplashLogin.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.SplashLogin.route) {
                SplashLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.SplashLogin.route) { inclusive = true }
                        }
                    },
                    onRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onJoinFrequency = {
                        navController.navigate(Screen.VipLounge.route)
                    }
                )
            }
            composable(Screen.Lobby.route) {
                LobbyScreen(
                    onJoinRoom = {
                        navController.navigate(Screen.VipLounge.route)
                    },
                    onOpenEchoChain = {
                        navController.navigate(Screen.EchoChain.route)
                    }
                )
            }
            composable(Screen.Connections.route) {
                ConnectionsScreen(
                    onOpenWhisper = {
                        navController.navigate(Screen.Whisper.route)
                    }
                )
            }
            composable(Screen.Aura.route) {
                AuraScreen()
            }
            composable(Screen.VipLounge.route) {
                VipLoungeScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Whisper.route) {
                WhisperScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.EchoChain.route) {
                EchoChainScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.SplashLogin.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Studio.route) {
                StudioScreen(
                    onBack = { navController.popBackStack() },
                    onSend = { navController.popBackStack() }
                )
            }
            composable(Screen.IncomingCall.route) {
                IncomingCallScreen(
                    onAccept = {
                        navController.navigate(Screen.VipLounge.route) {
                            popUpTo(Screen.IncomingCall.route) { inclusive = true }
                        }
                    },
                    onReject = { navController.popBackStack() }
                )
            }
            composable(Screen.Search.route) {
                SearchOverlayScreen(
                    onDismiss = { navController.popBackStack() },
                    onResultClick = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Navigation — hides gracefully when entering immersive screens
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomBar(
                currentRoute = currentRoute,
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
