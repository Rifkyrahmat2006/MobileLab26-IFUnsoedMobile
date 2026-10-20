package com.example.prak_pemmob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prak_pemmob.ui.screen.DaftarProductScreen
import com.example.prak_pemmob.ui.screen.DetailProductScreen
import com.example.prak_pemmob.ui.screen.HubungiKamiScreen
import com.example.prak_pemmob.ui.theme.PrakPemMobPer1Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakPemMobPer1Theme {
                val navController = rememberNavController()
                val productViewModel: com.example.prak_pemmob.ui.viewmodel.ProductViewModel = viewModel()
                NavHost(navController = navController, startDestination = "daftar_produk") {
                    composable(route = "daftar_produk") {
                        DaftarProductScreen(
                            navController = navController,
                            viewModel = productViewModel
                        )
                    }
                    composable(
                        route = "detail/{productId}",
                        arguments = listOf(navArgument(name = "productId") {
                            type = NavType.IntType
                        })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                        DetailProductScreen(
                            productId = productId,
                            navController = navController,
                            viewModel = productViewModel
                        )
                    }
                    composable(route = "hubungi_kami") {
                        HubungiKamiScreen(navController = navController)
                    }
                }
            }
        }
    }
}
