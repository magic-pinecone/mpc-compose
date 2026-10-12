package org.mpc.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private data class MaterialTimetableSwatch(
    val lightContainer: Color,
    val darkContainer: Color,
)

// More saturated Material swatches keep category fills visible in the timetable grid.
private val AndroidTimetableSwatches = listOf(
    MaterialTimetableSwatch(Color(0xFF64B5F6), Color(0xFF1565C0)), // Blue
    MaterialTimetableSwatch(Color(0xFF81C784), Color(0xFF2E7D32)), // Green
    MaterialTimetableSwatch(Color(0xFFFFB74D), Color(0xFFBF360C)), // Orange
    MaterialTimetableSwatch(Color(0xFFBA68C8), Color(0xFF6A1B9A)), // Purple
    MaterialTimetableSwatch(Color(0xFF4DB6AC), Color(0xFF00695C)), // Teal
    MaterialTimetableSwatch(Color(0xFF7986CB), Color(0xFF283593)), // Indigo
    MaterialTimetableSwatch(Color(0xFFF06292), Color(0xFFAD1457)), // Pink
    MaterialTimetableSwatch(Color(0xFF69F0AE), Color(0xFF006B4D)), // Mint
    MaterialTimetableSwatch(Color(0xFFE57373), Color(0xFFC62828)), // Red
    MaterialTimetableSwatch(Color(0xFF4DD0E1), Color(0xFF006064)), // Cyan
    MaterialTimetableSwatch(Color(0xFFFFF176), Color(0xFF6D4C00)), // Yellow
)

@Composable
actual fun platformTimetablePalette(darkTheme: Boolean): List<TimetableBlockColors> = AndroidTimetableSwatches.map { swatch ->
    val container = if (darkTheme) swatch.darkContainer else swatch.lightContainer
    TimetableBlockColors(
        container = container,
        content = container.bestContrastingTextColor(),
    )
}

private fun Color.bestContrastingTextColor(): Color {
    val relativeLuminance = luminance()
    val blackContrast = (relativeLuminance + WCAG_LUMINANCE_OFFSET) / WCAG_LUMINANCE_OFFSET
    val whiteContrast = (1f + WCAG_LUMINANCE_OFFSET) / (relativeLuminance + WCAG_LUMINANCE_OFFSET)
    return if (blackContrast >= whiteContrast) Color.Black else Color.White
}

private const val WCAG_LUMINANCE_OFFSET = 0.05f
