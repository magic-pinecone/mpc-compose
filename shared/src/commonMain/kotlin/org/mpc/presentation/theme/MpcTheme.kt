package org.mpc.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A small shadcn-inspired neutral palette shared by Material and adaptive Calf content.
 *
 * MaterialTheme remains the delivery mechanism so existing Material components and Calf
 * components receive the same semantic colors without coupling platform shells together.
 */
@Composable
fun MpcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)
private val Zinc50 = Color(0xFFFAFAFA)
private val Zinc100 = Color(0xFFF4F4F5)
private val Zinc200 = Color(0xFFE4E4E7)
private val Zinc300 = Color(0xFFD4D4D8)
private val Zinc400 = Color(0xFFA1A1AA)
private val Zinc500 = Color(0xFF71717A)
private val Zinc700 = Color(0xFF3F3F46)
private val Zinc800 = Color(0xFF27272A)
private val Zinc900 = Color(0xFF18181B)
private val Zinc950 = Color(0xFF09090B)
private val Red100 = Color(0xFFFEE2E2)
private val Red400 = Color(0xFFF87171)
private val Red600 = Color(0xFFDC2626)
private val Red900 = Color(0xFF7F1D1D)

private val LightColorScheme = lightColorScheme(
    primary = Zinc900,
    onPrimary = Zinc50,
    primaryContainer = Zinc100,
    onPrimaryContainer = Zinc900,
    inversePrimary = Zinc200,
    secondary = Zinc100,
    onSecondary = Zinc900,
    secondaryContainer = Zinc100,
    onSecondaryContainer = Zinc900,
    tertiary = Zinc200,
    onTertiary = Zinc900,
    tertiaryContainer = Zinc100,
    onTertiaryContainer = Zinc900,
    background = Zinc50,
    onBackground = Zinc950,
    surface = White,
    onSurface = Zinc950,
    surfaceVariant = Zinc100,
    onSurfaceVariant = Zinc500,
    inverseSurface = Zinc950,
    inverseOnSurface = Zinc50,
    error = Red600,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red900,
    outline = Zinc300,
    outlineVariant = Zinc200,
    scrim = Black,
    surfaceBright = White,
    surfaceDim = Zinc200,
    surfaceContainer = Zinc50,
    surfaceContainerHigh = Zinc100,
    surfaceContainerHighest = Zinc200,
    surfaceContainerLow = Zinc50,
    surfaceContainerLowest = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Zinc50,
    onPrimary = Zinc950,
    primaryContainer = Zinc800,
    onPrimaryContainer = Zinc50,
    inversePrimary = Zinc800,
    secondary = Zinc800,
    onSecondary = Zinc50,
    secondaryContainer = Zinc800,
    onSecondaryContainer = Zinc50,
    tertiary = Zinc700,
    onTertiary = Zinc50,
    tertiaryContainer = Zinc800,
    onTertiaryContainer = Zinc50,
    background = Zinc950,
    onBackground = Zinc50,
    surface = Zinc950,
    onSurface = Zinc50,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Zinc400,
    inverseSurface = Zinc50,
    inverseOnSurface = Zinc900,
    error = Red400,
    onError = White,
    errorContainer = Red900,
    onErrorContainer = Red100,
    outline = Zinc700,
    outlineVariant = Zinc800,
    scrim = Black,
    surfaceBright = Zinc800,
    surfaceDim = Zinc950,
    surfaceContainer = Zinc900,
    surfaceContainerHigh = Zinc800,
    surfaceContainerHighest = Zinc800,
    surfaceContainerLow = Zinc950,
    surfaceContainerLowest = Black,
)
