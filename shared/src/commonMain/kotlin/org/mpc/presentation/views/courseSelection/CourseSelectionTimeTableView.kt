package org.mpc.presentation.views.courseSelection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.domain.model.state.CoursePlanState

@Composable
fun CourseSelectionTimetableView(
    state: CoursePlanState,
    displayWeekends: Boolean,
    modifier: Modifier,
) {
    when(state) {
        is CoursePlanState.Failure -> CourseSelectionTimetableFailureView(modifier, state.cause)
        CoursePlanState.Loading -> CourseSelectionTimetableLoadingView(modifier)
        is CoursePlanState.Success -> CourseSelectionTimetableSuccessView(
            snapshot = state.snapshot,
            displayWeekends = displayWeekends,
            modifier = modifier,
        )
    }
}