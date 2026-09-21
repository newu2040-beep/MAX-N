package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.ui.screens.CustomToolCreatorScreen
import com.example.ui.screens.DailyMemoryScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PrdGeneratorScreen
import com.example.ui.screens.ProductivityScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PromptStudioScreen
import com.example.ui.screens.ToolCombinationsScreen
import com.example.ui.screens.ToolRunnerScreen
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
    object Explore : Screen("explore")
    object Writing : Screen("writing")
    object Images : Screen("images")
    object Prd : Screen("prd")
    object Productivity : Screen("productivity")
    object Code : Screen("code")
    object Library : Screen("library")
    object Profile : Screen("profile")

    object ToolRunner : Screen("tool_runner/{toolId}?input={input}") {
        fun createRoute(toolId: String, input: String? = null): String {
            return if (!input.isNullOrBlank()) {
                val encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                "tool_runner/$toolId?input=$encoded"
            } else {
                "tool_runner/$toolId"
            }
        }
    }
    object ToolCombinations : Screen("tool_combinations")
    object DailyMemory : Screen("daily_memory")
    object CustomToolCreator : Screen("custom_tool_creator")
}

@Composable
fun AppNavigation(
    prefsManager: UserPreferencesManager,
    database: AppDatabase,
    aiService: AIProviderService,
    sharedTextFromIntent: String? = null
) {
    val navController = rememberNavController()
    val settings by prefsManager.settings.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Show bottom bar on primary dashboard screens
    val showBottomBar = currentRoute in listOf("home", "explore", "library", "profile")

    val startDestination = if (settings.hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MAXBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { target ->
                        val destinationRoute = when (target) {
                            "home" -> Screen.Home.route
                            "explore" -> Screen.Explore.route
                            "library" -> Screen.Library.route
                            "profile" -> Screen.Profile.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(destinationRoute) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
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
                    customToolDao = database.customToolDao(),
                    sharedTextFromIntent = sharedTextFromIntent,
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
                    onNavigateToToolRunner = { toolId, input ->
                        navController.navigate(Screen.ToolRunner.createRoute(toolId, input))
                    },
                    onNavigateToDailyMemory = { navController.navigate(Screen.DailyMemory.route) },
                    onNavigateToToolCombinations = { navController.navigate(Screen.ToolCombinations.route) },
                    onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                    onToggleThinking = { enabled -> prefsManager.toggleThinkingMode(enabled) }
                )
            }

            composable(Screen.Explore.route) {
                ExploreScreen(
                    settings = settings,
                    customToolDao = database.customToolDao(),
                    onNavigateToToolRunner = { toolId ->
                        navController.navigate(Screen.ToolRunner.createRoute(toolId, null))
                    },
                    onNavigateToDailyMemory = { navController.navigate(Screen.DailyMemory.route) },
                    onNavigateToToolCombinations = { navController.navigate(Screen.ToolCombinations.route) },
                    onNavigateToCreateCustomTool = { navController.navigate(Screen.CustomToolCreator.route) }
                )
            }

            // Central Tool Runner Route for all 12 tool types
            composable(
                route = "tool_runner/{toolId}?input={input}",
                arguments = listOf(
                    navArgument("toolId") { type = NavType.StringType },
                    navArgument("input") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments?.getString("toolId") ?: "bill_expense_organizer"
                val encodedInput = backStackEntry.arguments?.getString("input")
                val decodedInput = if (!encodedInput.isNullOrBlank()) {
                    runCatching { URLDecoder.decode(encodedInput, StandardCharsets.UTF_8.toString()) }.getOrDefault(encodedInput)
                } else null

                val customTools by database.customToolDao().getAllCustomTools().collectAsState(initial = emptyList())
                val customEntity = customTools.find { it.id == toolId }

                ToolRunnerScreen(
                    toolId = toolId,
                    initialInput = decodedInput,
                    settings = settings,
                    aiService = aiService,
                    libraryDao = database.libraryDao(),
                    dailyMemoryDao = database.dailyMemoryDao(),
                    customToolEntity = customEntity,
                    onBack = { navController.popBackStack() },
                    onChainToTool = { nextToolId, text ->
                        navController.navigate(Screen.ToolRunner.createRoute(nextToolId, text))
                    }
                )
            }

            // Tool Combinations Screen (Feature #10)
            composable(Screen.ToolCombinations.route) {
                ToolCombinationsScreen(
                    settings = settings,
                    aiService = aiService,
                    onBack = { navController.popBackStack() }
                )
            }

            // Daily AI Memory Screen (Feature #7)
            composable(Screen.DailyMemory.route) {
                DailyMemoryScreen(
                    dailyMemoryDao = database.dailyMemoryDao(),
                    onBack = { navController.popBackStack() }
                )
            }

            // Custom Tool Creator Screen (Feature #11)
            composable(Screen.CustomToolCreator.route) {
                CustomToolCreatorScreen(
                    customToolDao = database.customToolDao(),
                    onBack = { navController.popBackStack() },
                    onToolCreated = { newToolId ->
                        navController.popBackStack()
                        navController.navigate(Screen.ToolRunner.createRoute(newToolId, null))
                    }
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
                    prefsManager = prefsManager,
                    aiService = aiService
                )
            }
        }
    }
}
