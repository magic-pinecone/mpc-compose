package org.mpc.presentationn.views.courseSelection

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.domain.model.state.CourseLoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSearchResultView(
    modifier: Modifier,
    courseLoadState: CourseLoadState
) {
    when(courseLoadState) {
        is CourseLoadState.Success -> CourseResultSuccessView(modifier, courseLoadState.courses)
        is CourseLoadState.Failure -> CourseResultFailureView(modifier, courseLoadState.error)
        CourseLoadState.Loading -> CourseResultLoadingView(modifier)
    }
}


