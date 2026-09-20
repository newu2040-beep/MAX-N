package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ai.AIProviderService
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.components.MAXBottomBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodeDevScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PrdGeneratorScreen
import com.example.ui.screens.ProductivityScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PromptStudioScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.WritingToolsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Chat : Screen("chat?prompt={prompt}") {
        fun createRoute(prompt: String? = null): String {
            return if (!prompt.isNullOrBlank()) {
                val encoded = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString())
                "chat?prompt=$encoded"
            } else {
                "chat"
            }
        }
    }
    object Writing : Screen("writing")
    object Images : Screen("images")
    object Prd : Screen("prd")
    object Productivity : Screen("productivity")
    object Code : Screen("code")
    object Library : Screen("library")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(
    prefsManager: UserPreferencesManager,
    database: AppDatabase,
    aiService: AIProviderService
) {
    val navController = rememberNavController()
    val settings by prefsManager.settings.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Show bottom bar only on primary dashboard screens
    val showBottomBar = currentRoute in listOf("home", "library", "profile")

    val startDestination = if (settings.hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MAXBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { target ->
                        when (target) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            "explore" -> navController.navigate(Screen.Writing.route)
                            "library" -> navController.navigate(Screen.Library.route) {
                                launchSingleTop = true
                            }
                            "profile" -> navController.navigate(Screen.Profile.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        prefsManager.setOnboardingCompleted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onSignIn = {
                        prefsManager.setOnboardingCompleted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    settings = settings,
                    onNavigateToChat = { prompt ->
                        navController.navigate(Screen.Chat.createRoute(prompt))
                    },
                    onNavigateToWriting = { navController.navigate(Screen.Writing.route) },
                    onNavigateToImages = { navController.navigate(Screen.Images.route) },
                    onNavigateToProductivity = { navController.navigate(Screen.Productivity.route) },
                    onNavigateToCode = { navController.navigate(Screen.Code.route) },
                    onNavigateToPrd = { navController.navigate(Screen.Prd.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onToggleThinking = { enabled -> prefsManager.toggleThinkingMode(enabled) }
                )
            }

            composable(
                route = "chat?prompt={prompt}",
                arguments = listOf(
                    navArgument("prompt") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val encodedPrompt = backStackEntry.arguments?.getString("prompt")
                val initialPrompt = if (!encodedPrompt.isNullOrBlank()) {
                    runCatching {
                        URLDecoder.decode(encodedPrompt, StandardCharsets.UTF_8.toString())
                    }.getOrDefault(encodedPrompt)
                } else null

                ChatScreen(
                    settings = settings,
                    aiService = aiService,
                    database = database,
                    initialPrompt = initialPrompt,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Writing.route) {
                WritingToolsScreen(
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Images.route) {
                PromptStudioScreen(
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Prd.route) {
                PrdGeneratorScreen(
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Productivity.route) {
                ProductivityScreen(
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Code.route) {
                CodeDevScreen(
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    database = database
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    settings = settings,
                    prefsManager = prefsManager
                )
            }
        }
    }
}
