package org.mpc.presentation.views.courseSelection

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.mpc.domain.model.state.CourseSearchError

@Composable
fun CourseResultFailureView(modifier: Modifier, error: CourseSearchError) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Failed to load: $error")
    }
}

