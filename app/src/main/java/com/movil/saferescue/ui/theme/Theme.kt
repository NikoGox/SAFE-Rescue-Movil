package com.movil.saferescue.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SRPrimaryBlue, // El azul principal
    secondary = SRSecondaryRed, // El rojo de acento
    tertiary = Color.Black, // Color de texto/ícono terciario
    background = SRBackgroundLight, // Fondo claro de la marca
    surface = Color.White,
    onPrimary = Color.White, // Color del texto/icono sobre el color primario
    onBackground = Color(0xFF1C1B1F), // Color del texto/icono sobre el fondo
)

private val DarkColorScheme = LightColorScheme

@Composable
fun SAFERescueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}