package se.fejes.compositionplayerbugrepro.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006689),
    onPrimary = Color.White,
    secondary = Color(0xFF4D616C),
    tertiary = Color(0xFF505F71),
    background = Color(0xFF929292)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AD0FF),
    onPrimary = Color(0xFF003547),
    secondary = Color(0xFFB1CBD0),
    tertiary = Color(0xFFBAC8E1),
    background = Color(0xFF323232)
)

@Composable
fun CompositionPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}