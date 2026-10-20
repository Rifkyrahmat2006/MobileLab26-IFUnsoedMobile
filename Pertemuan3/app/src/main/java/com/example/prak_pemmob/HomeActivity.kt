package com.example.prak_pemmob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prak_pemmob.ui.screen.DaftarProductScreen
import com.example.prak_pemmob.ui.theme.PrakPemMobPer1Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrakPemMobPer1Theme {
                DaftarProductScreen()
            }
        }
    }
}