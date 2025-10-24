package com.movil.saferescue.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores personalizada y ÚNICA para toda la aplicación.
private val AppColorScheme = lightColorScheme(
    primary = SRPrimaryBlue,          // El azul principal de la marca.
    secondary = SRSecondaryRed,       // El rojo de acento.
    tertiary = Color.Black,           // Color de texto/ícono terciario.
    background = SRBackgroundLight,   // Fondo claro de la marca.
    surface = Color.White,            // Color para superficies como Cards, Menus.
    onPrimary = Color.White,          // Texto/ícono sobre el color primario.
    onBackground = Color(0xFF1C1B1F), // Texto/ícono sobre el color de fondo.
)

@Composable
fun SAFERescueTheme(
    // Los parámetros de darkTheme y dynamicColor se ignoran para forzar un tema consistente.
    darkTheme: Boolean = false, // No se usará
    dynamicColor: Boolean = false, // No se usará
    content: @Composable () -> Unit
) {
    // Se aplica siempre la misma paleta de colores (AppColorScheme),
    // ignorando la configuración del sistema (tema oscuro o colores dinámicos).
    val colorScheme = AppColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
