package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.courseDetails.CourseDetailsBottomSheet
import org.mpc.presentation.views.courseDetails.CourseDetailsSelection
import org.mpc.presentation.views.coursePlanning.CoursePlanningTimetableView
import org.mpc.presentation.views.coursePlanning.SelectedCoursesBottomSheet

@Composable
fun CoursePlanningTimetableViewBinding(
    planBridge: CoursePlanBridge,
    courseRepository: CourseRepository,
) {
    val planViewModel: CoursePlanViewModel = metroViewModel()

    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCourse by remember { mutableStateOf<CourseDetailsSelection?>(null) }
    var isShowingSelectedCourses by remember { mutableStateOf(false) }
    val selectedCourses =
        (planUiState as? CoursePlanUiState.Success)
            ?.plan
            ?.selectedCourses
            ?.values
            ?.sortedBy { course -> course.title }
            .orEmpty()

    SideEffect {
        planBridge.updateSelectedCourses(selectedCourses)
    }

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.saveRequests.collect {
            planViewModel.savePlan()
        }
    }

    LaunchedEffect(planBridge) {
        planBridge.selectedCoursesSheetRequests.collect {
            isShowingSelectedCourses = true
        }
    }

    CoursePlanningTimetableView(
        uiState = planUiState,
        modifier = Modifier.fillMaxSize(),
        onCourseClick = { semester, course ->
            selectedCourse = CourseDetailsSelection(semester, course)
        },
    )

    selectedCourse?.let { selection ->
        CourseDetailsBottomSheet(
            selection = selection,
            courseRepository = courseRepository,
            onDismissRequest = { selectedCourse = null },
            planViewModel = planViewModel,
        )
    }

    if (isShowingSelectedCourses) {
        SelectedCoursesBottomSheet(
            courses = selectedCourses,
            onDismissRequest = { isShowingSelectedCourses = false },
        )
    }
}
