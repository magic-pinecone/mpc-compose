package org.mpc.presentation.views.courseSelection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.domain.model.state.CourseLoadState

@Composable
fun CourseSearchResultView(
    modifier: Modifier = Modifier,
    courseLoadState: CourseLoadState
) {
    when(courseLoadState) {
        is CourseLoadState.Success -> CourseResultSuccessView(modifier, courseLoadState.courses)
        is CourseLoadState.Failure -> CourseResultFailureView(modifier, courseLoadState.error)
        CourseLoadState.Loading -> CourseResultLoadingView(modifier)
    }
}


