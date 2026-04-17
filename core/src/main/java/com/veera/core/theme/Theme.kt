package com.veera.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF64B5F6)
val PrimaryDark = Color(0xFF1976D2)
val Background = Color(0xFF0D1B2A)
val Surface = Color(0xFF1B263B)

val ButtonGradientStart = Color(0xFF4B61D1)
val ButtonGradientEnd = Color(0xFF7B8CFE)
val TextSecondary = Color(0xFF7B8CFE)
val IllustrationBlue = Color(0xFF46C2E2)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Color(0xFF415A77),
    tertiary = Color(0xFF778DA9),
    background = Background,
    surface = Surface
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Color(0xFF415A77),
    tertiary = Color(0xFF778DA9)
)

@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
