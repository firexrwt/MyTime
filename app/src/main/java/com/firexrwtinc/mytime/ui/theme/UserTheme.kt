package com.firexrwtinc.mytime.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firexrwtinc.mytime.data.model.AppSettings
import com.firexrwtinc.mytime.data.model.ThemeMode
import com.firexrwtinc.mytime.hexToColor
import com.firexrwtinc.mytime.ui.settings.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UserAwareTheme(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    // Определяем, нужна ли темная тема
    val systemInDarkTheme = isSystemInDarkTheme()
    val shouldUseDarkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }
    
    // Создаем кастомную цветовую схему с пользовательским акцентным цветом
    val accentColor = hexToColor(settings.accentColor)
    
    val customColorScheme = if (shouldUseDarkTheme) {
        createDarkColorScheme(accentColor)
    } else {
        createLightColorScheme(accentColor)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = customColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !shouldUseDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = customColorScheme,
        typography = Typography,
        content = content
    )
}

private fun createLightColorScheme(accentColor: Color): ColorScheme {
    return lightColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.1f),
        onPrimaryContainer = accentColor,
        secondary = accentColor.copy(alpha = 0.8f),
        onSecondary = Color.White,
        secondaryContainer = accentColor.copy(alpha = 0.2f),
        onSecondaryContainer = accentColor,
        tertiary = accentColor.copy(alpha = 0.6f),
        onTertiary = Color.White,
        tertiaryContainer = accentColor.copy(alpha = 0.15f),
        onTertiaryContainer = accentColor,
        error = Color(0xFFB3261E),
        onError = Color.White,
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF313033),
        inverseOnSurface = Color(0xFFF4EFF4),
        inversePrimary = accentColor.copy(alpha = 0.8f)
    )
}

private fun createDarkColorScheme(accentColor: Color): ColorScheme {
    return darkColorScheme(
        primary = accentColor.copy(alpha = 0.9f),
        onPrimary = Color.Black,
        primaryContainer = accentColor.copy(alpha = 0.3f),
        onPrimaryContainer = accentColor,
        secondary = accentColor.copy(alpha = 0.7f),
        onSecondary = Color.Black,
        secondaryContainer = accentColor.copy(alpha = 0.25f),
        onSecondaryContainer = accentColor,
        tertiary = accentColor.copy(alpha = 0.5f),
        onTertiary = Color.Black,
        tertiaryContainer = accentColor.copy(alpha = 0.2f),
        onTertiaryContainer = accentColor,
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = accentColor
    )
}