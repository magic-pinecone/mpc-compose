package org.mpc.presentation.views.courseSelection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mpc.domain.model.entity.CourseSerialNo
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.repository.CourseLoadState

@Composable
fun CourseSearchResultView(
    modifier: Modifier = Modifier,
    courseLoadState: CourseLoadState,
    selectedCourseSerialNumbers: Set<CourseSerialNo>,
    onToggleCourse: (CourseSummary) -> Unit,
) {
    when (courseLoadState) {
        is CourseLoadState.Success -> CourseResultSuccessView(
            modifier = modifier,
            courseResult = courseLoadState.courses,
            selectedCourseSerialNumbers = selectedCourseSerialNumbers,
            onCourseToggle = onToggleCourse,
        )
        is CourseLoadState.Failure -> CourseResultFailureView(modifier, courseLoadState.error)
        CourseLoadState.Loading -> CourseResultLoadingView(modifier)
    }
}

