package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.ColorSchemeMode

@Composable
fun Material3AppTheme(content: @Composable () -> Unit) {
    val isDark = when (AppConfig.colorSchemeMode) {
        ColorSchemeMode.Light -> false
        ColorSchemeMode.Dark -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
        content = content
    )
}
