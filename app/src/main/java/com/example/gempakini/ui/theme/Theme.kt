package com.example.gempakini.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SeismicRed80,
    secondary = OceanBlue80,
    tertiary = EarthAmber80,
    background = DarkSurface,
    surface = DarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SeismicRed40,
    secondary = OceanBlue40,
    tertiary = EarthAmber40,
    background = LightSurface,
    surface = LightSurface
)

@Composable
fun GempaKiniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // dynamicColor sengaja tidak dipakai agar palet custom di atas konsisten di semua device/API level.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
