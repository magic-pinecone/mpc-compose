package org.mpc.presentation.views.courseSelection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.presentation.state.CourseSearchResultUiState

@Composable
fun CourseSearchResultView(
    modifier: Modifier = Modifier,
    uiState: CourseSearchResultUiState,
    selectedCourseSerialNumbers: Set<CourseSerialNo>,
    onToggleCourse: (CourseSummary) -> Unit,
) {
    when (uiState) {
        is CourseSearchResultUiState.Success -> {
            CourseSearchResultSuccessView(
                modifier = modifier,
                courseResult = uiState.result,
                selectedCourseSerialNumbers = selectedCourseSerialNumbers,
                onCourseToggle = onToggleCourse,
            )
        }

        is CourseSearchResultUiState.Failure -> {
            CourseSearchResultFailureView(modifier, uiState.error)
        }

        CourseSearchResultUiState.Loading -> {
            CourseSearchResultLoadingView(modifier)
        }
    }
}
