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
import org.mpc.bridge.CourseSearchBridge
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.views.coursePlanning.CourseSearchResultView
import org.mpc.presentation.views.coursePlanning.SelectedCoursesBottomSheet

@Composable
fun CourseCatalogViewBinding(
    bridge: CourseSearchBridge,
    planBridge: CoursePlanBridge,
) {
    val searchViewModel: CourseSearchViewModel = metroViewModel()
    val planViewModel: CoursePlanViewModel = metroViewModel()

    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    var isShowingSelectedCourses by remember { mutableStateOf(false) }

    val selectedCourseSerialNumbers =
        when (val current = planUiState) {
            CoursePlanUiState.Loading -> emptySet()
            is CoursePlanUiState.Failure -> emptySet()
            is CoursePlanUiState.Success -> current.plan.selectedCourses.keys
        }
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

    LaunchedEffect(bridge, searchViewModel) {
        bridge.sendRequests.collect { (semester, query) ->
            searchViewModel.updateQuery(semester, query)
            searchViewModel.onSearch()
        }
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

    CourseSearchResultView(
        modifier = Modifier.fillMaxSize(),
        uiState = searchUiState.result,
        selectedCourseSerialNumbers = selectedCourseSerialNumbers,
        onToggleCourse = planViewModel::toggleCourse,
    )

    if (isShowingSelectedCourses) {
        SelectedCoursesBottomSheet(
            courses = selectedCourses,
            onDismissRequest = { isShowingSelectedCourses = false },
        )
    }
}
