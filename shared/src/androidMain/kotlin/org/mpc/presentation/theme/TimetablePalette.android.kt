package org.mpc.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private data class GoogleCalendarEventSwatch(
    val lightModeFill: Color,
    val darkModeFill: Color,
)

// Current Google Calendar Android event-color pairs, ordered by the stable timetable hue slots.
private val AndroidTimetableSwatches = listOf(
    GoogleCalendarEventSwatch(Color(0xFF3F51B5), Color(0xFF5484ED)), // Blue: Blueberry
    GoogleCalendarEventSwatch(Color(0xFF0B8043), Color(0xFF51B749)), // Green: Basil
    GoogleCalendarEventSwatch(Color(0xFFF4511E), Color(0xFFFFB878)), // Orange: Tangerine
    GoogleCalendarEventSwatch(Color(0xFF8E24AA), Color(0xFFDBADFF)), // Purple: Grape
    GoogleCalendarEventSwatch(Color(0xFF616161), Color(0xFFE1E1E1)), // Teal: Graphite for MN's neutral
    GoogleCalendarEventSwatch(Color(0xFF7986CB), Color(0xFFA4BDFC)), // Indigo: Lavender
    GoogleCalendarEventSwatch(Color(0xFFE67C73), Color(0xFFFF887C)), // Pink: Flamingo
    GoogleCalendarEventSwatch(Color(0xFF33B679), Color(0xFF7AE7BF)), // Mint: Sage
    GoogleCalendarEventSwatch(Color(0xFFD50000), Color(0xFFDC2127)), // Red: Tomato
    GoogleCalendarEventSwatch(Color(0xFF039BE5), Color(0xFF46D6DB)), // Cyan: Peacock
    GoogleCalendarEventSwatch(Color(0xFFF6BF26), Color(0xFFFBD75B)), // Yellow: Banana
)

@Composable
actual fun platformTimetablePalette(darkTheme: Boolean): List<TimetableBlockColors> = AndroidTimetableSwatches.map { swatch ->
    TimetableBlockColors(
        container = if (darkTheme) swatch.darkModeFill else swatch.lightModeFill,
        content = if (darkTheme) Color.Black else Color.White,
    )
}
