package mg.sitraka.wushubase

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFC62828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFFFB300),
    onSecondary = Color(0xFF2B1D00),
    secondaryContainer = Color(0xFFFFEFC2),
    onSecondaryContainer = Color(0xFF2B1D00),
    background = Color(0xFFFFFBF2),
    onBackground = Color(0xFF211A14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF211A14),
    surfaceVariant = Color(0xFFF7EBD4),
    onSurfaceVariant = Color(0xFF50453A),
    outline = Color(0xFF8A7B6A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6659),
    onPrimary = Color(0xFF410002),
    primaryContainer = Color(0xFF8E1B1B),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3B2A00),
    secondaryContainer = Color(0xFF5A4300),
    onSecondaryContainer = Color(0xFFFFE8A3),
    background = Color(0xFF14100E),
    onBackground = Color(0xFFF0E6DC),
    surface = Color(0xFF1E1816),
    onSurface = Color(0xFFF0E6DC),
    surfaceVariant = Color(0xFF2E2420),
    onSurfaceVariant = Color(0xFFD6C6B8),
    outline = Color(0xFF9A8A7A)
)

@Composable
fun WushuTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
