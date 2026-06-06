package com.example.aveslens.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aveslens.ui.screens.auth.AuthScreen
import com.example.aveslens.ui.screens.detail.DetailScreen
import com.example.aveslens.ui.screens.explorer.ExplorerScreen
import com.example.aveslens.ui.screens.form.FormScreen
import com.example.aveslens.ui.screens.home.HomeScreen
import com.example.aveslens.ui.screens.profile.ProfileScreen

@Composable
fun AvesLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.AUTH,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) { backStackEntry ->
            // Refresh list when returning from Form after a successful save
            val savedStateHandle = backStackEntry.savedStateHandle
            val observationSaved by savedStateHandle.getStateFlow("observation_saved", false)
                .collectAsState()

            HomeScreen(
                onNavigateToForm = { navController.navigate(Routes.form()) },
                onNavigateToDetail = { obsId -> navController.navigate(Routes.detail(obsId)) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToExplorer = { navController.navigate(Routes.EXPLORER) },
                observationSaved = observationSaved,
                onObservationSavedConsumed = { savedStateHandle["observation_saved"] = false },
            )
        }

        composable(
            route = Routes.FORM,
            arguments = listOf(
                navArgument("observationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            FormScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("observation_saved", true)
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("observationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val observationUpdated by savedStateHandle.getStateFlow("observation_saved", false)
                .collectAsState()

            DetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { obsId ->
                    navController.navigate(Routes.form(obsId))
                },
                onDeleteSuccess = {
                    navController.getBackStackEntry(Routes.HOME)
                        .savedStateHandle["observation_saved"] = true
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                observationUpdated = observationUpdated,
                onObservationUpdatedConsumed = { savedStateHandle["observation_saved"] = false },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.EXPLORER) {
            ExplorerScreen(
                onNavigateToJournal = { navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = false }
                } },
                onNavigateToForm = { navController.navigate(Routes.form()) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
            )
        }
    }
}
