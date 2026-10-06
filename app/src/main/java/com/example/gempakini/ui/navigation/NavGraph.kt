package com.example.gempakini.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gempakini.ui.detail.DetailScreen
import com.example.gempakini.ui.home.HomeScreen
import com.example.gempakini.ui.home.HomeViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Satu HomeViewModel di-scope ke NavHost, dipakai Home & Detail (lihat Design.md §6).
    val viewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onGempaClick = { index ->
                    navController.navigate(Screen.Detail.createRoute(index))
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            DetailScreen(
                gempa = viewModel.getGempaAt(index),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
