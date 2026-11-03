package com.yayadile.ricecheck.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.screens.AnalyzingScreen
import com.yayadile.ricecheck.ui.screens.CameraScreen
import com.yayadile.ricecheck.ui.screens.HistoryDetailScreen
import com.yayadile.ricecheck.ui.screens.HistoryScreen
import com.yayadile.ricecheck.ui.screens.HomeScreen
import com.yayadile.ricecheck.ui.screens.OnboardingScreen
import com.yayadile.ricecheck.ui.screens.ResultScreen
import com.yayadile.ricecheck.ui.theme.RiceColors
import kotlinx.coroutines.flow.first

/**
 * Decides the first destination from the single DataStore key (onboarding_completed)
 * before the NavHost is created, so there is no onboarding flash on later launches.
 */
@Composable
fun RiceCheckRoot(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val completed = runCatching {
            container.onboardingPrefs.onboardingCompleted.first()
        }.getOrDefault(false)
        startDestination = if (completed) Routes.HOME else Routes.ONBOARDING
    }

    val destination = startDestination
    if (destination == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(RiceColors.Background)
        )
    } else {
        RiceCheckNavGraph(startDestination = destination)
    }
}

@Composable
fun RiceCheckNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onScan = { navController.navigate(Routes.CAMERA) },
                onHistory = { navController.switchTab(Routes.HISTORY) },
                onOpenScan = { scanId -> navController.navigate(Routes.result(scanId)) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onCaptured = { photoPath, price ->
                    navController.navigate(Routes.analyzing(photoPath, price))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ANALYZING,
            arguments = listOf(
                navArgument(Routes.ARG_PHOTO_URI) { type = NavType.StringType },
                navArgument(Routes.ARG_PRICE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            AnalyzingScreen(
                photoUri = backStackEntry.arguments?.getString(Routes.ARG_PHOTO_URI).orEmpty(),
                price = backStackEntry.arguments?.getString(Routes.ARG_PRICE)?.toIntOrNull(),
                onCompleted = { scanId ->
                    navController.navigate(Routes.result(scanId)) {
                        popUpTo(Routes.CAMERA) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument(Routes.ARG_SCAN_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getLong(Routes.ARG_SCAN_ID) ?: 0L
            ResultScreen(
                scanId = scanId,
                onBack = { navController.popBackStack() },
                onScanAgain = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onHistory = { navController.switchTab(Routes.HISTORY) }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onScan = { navController.navigate(Routes.CAMERA) },
                onHome = { navController.switchTab(Routes.HOME) },
                onOpenScan = { scanId -> navController.navigate(Routes.historyDetail(scanId)) }
            )
        }

        composable(
            route = Routes.HISTORY_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_SCAN_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getLong(Routes.ARG_SCAN_ID) ?: 0L
            HistoryDetailScreen(
                scanId = scanId,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { inclusive = route == Routes.HOME }
        launchSingleTop = true
    }
}
