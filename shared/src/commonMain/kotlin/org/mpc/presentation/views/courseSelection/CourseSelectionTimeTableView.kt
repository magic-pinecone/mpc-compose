package org.mpc.presentation.views.courseSelection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.presentation.state.CoursePlanUiState

@Composable
fun CourseSelectionTimetableView(
    uiState: CoursePlanUiState,
    modifier: Modifier,
) {
    when (uiState) {
        is CoursePlanUiState.Failure -> {
            CourseSelectionTimetableFailureView(modifier, uiState.cause)
        }

        CoursePlanUiState.Loading -> {
            CourseSelectionTimetableLoadingView(modifier)
        }

        is CoursePlanUiState.Success -> {
            CourseSelectionTimetableSuccessView(
                plan = uiState.plan,
                modifier = modifier,
            )
        }
    }
}
