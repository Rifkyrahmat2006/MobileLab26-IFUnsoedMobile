package com.example.gempakini.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{index}") {
        fun createRoute(index: Int) = "detail/$index"
    }
}
