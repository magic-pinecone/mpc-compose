package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.bridge.CourseSearchBridge
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.views.coursePlanning.CourseSearchResultView

@Composable
fun CourseCatalogViewBinding(
    bridge: CourseSearchBridge,
    planBridge: CoursePlanBridge,
) {
    val searchViewModel: CourseSearchViewModel = metroViewModel()
    val planViewModel: CoursePlanViewModel = metroViewModel()

    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()

    val selectedCourseSerialNumbers =
        when (val current = planUiState) {
            CoursePlanUiState.Loading -> emptySet()
            is CoursePlanUiState.Failure -> emptySet()
            is CoursePlanUiState.Success -> current.plan.selectedCourses.keys
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

    CourseSearchResultView(
        modifier = Modifier.fillMaxSize(),
        uiState = searchUiState.result,
        selectedCourseSerialNumbers = selectedCourseSerialNumbers,
        onToggleCourse = planViewModel::toggleCourse,
    )
}
