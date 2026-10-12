package org.mpc.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Fixed Material 3-style tonal pairs keep the 11 course hues distinct across user themes.
private val LightTimetablePalette = listOf(
    TimetableBlockColors(Color(0xFFD3E3FD), Color(0xFF041E49)), // Blue
    TimetableBlockColors(Color(0xFFC4EED0), Color(0xFF072711)), // Green
    TimetableBlockColors(Color(0xFFFCE8D5), Color(0xFF512400)), // Orange
    TimetableBlockColors(Color(0xFFEADDFF), Color(0xFF21005D)), // Purple
    TimetableBlockColors(Color(0xFFC2F2F4), Color(0xFF002021)), // Teal
    TimetableBlockColors(Color(0xFFDEE0FF), Color(0xFF101345)), // Indigo
    TimetableBlockColors(Color(0xFFFFD8E4), Color(0xFF31111D)), // Pink
    TimetableBlockColors(Color(0xFFC8F4D2), Color(0xFF00210B)), // Mint
    TimetableBlockColors(Color(0xFFFFDAD6), Color(0xFF410002)), // Red
    TimetableBlockColors(Color(0xFFC2F2FB), Color(0xFF001F26)), // Cyan
    TimetableBlockColors(Color(0xFFF9E2A3), Color(0xFF241A00)), // Yellow
)

private val DarkTimetablePalette = listOf(
    TimetableBlockColors(Color(0xFF0842A0), Color(0xFFD3E3FD)), // Blue
    TimetableBlockColors(Color(0xFF0F5223), Color(0xFFC4EED0)), // Green
    TimetableBlockColors(Color(0xFF7B3600), Color(0xFFFCE8D5)), // Orange
    TimetableBlockColors(Color(0xFF4F378B), Color(0xFFEADDFF)), // Purple
    TimetableBlockColors(Color(0xFF004F50), Color(0xFFC2F2F4)), // Teal
    TimetableBlockColors(Color(0xFF363A77), Color(0xFFDEE0FF)), // Indigo
    TimetableBlockColors(Color(0xFF633B48), Color(0xFFFFD8E4)), // Pink
    TimetableBlockColors(Color(0xFF005323), Color(0xFFC8F4D2)), // Mint
    TimetableBlockColors(Color(0xFF93000A), Color(0xFFFFDAD6)), // Red
    TimetableBlockColors(Color(0xFF004E5E), Color(0xFFC2F2FB)), // Cyan
    TimetableBlockColors(Color(0xFF5D4300), Color(0xFFF9E2A3)), // Yellow
)

@Composable
actual fun platformTimetablePalette(darkTheme: Boolean): List<TimetableBlockColors> = if (darkTheme) DarkTimetablePalette else LightTimetablePalette
