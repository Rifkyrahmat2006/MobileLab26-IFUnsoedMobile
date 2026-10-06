package com.example.gempakini.ui.theme

import androidx.compose.ui.graphics.Color

/** Extension function: tentukan warna badge berdasarkan besar magnitudo. */
fun String.magnitudeColor(): Color {
    val value = this.toFloatOrNull() ?: return Color.Gray
    return when {
        value >= 6.0f -> Color(0xFFD32F2F) // merah — besar
        value >= 5.0f -> Color(0xFFE8734A) // oranye — sedang-besar
        value >= 4.0f -> Color(0xFFF9A825) // kuning — sedang
        else -> Color(0xFF2E7D32)          // hijau — kecil
    }
}
