package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.coursePlanning.CoursePlanningTimetableView

@Composable
fun CoursePlanningTimetableViewBinding(planBridge: CoursePlanBridge) {
    val planViewModel: CoursePlanViewModel = metroViewModel()

    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.saveRequests.collect {
            planViewModel.savePlan()
        }
    }

    SideEffect {
        val selectedCourses =
            (planUiState as? CoursePlanUiState.Success)
                ?.plan
                ?.selectedCourses
                ?.values
                ?.sortedBy { course -> course.title }
                .orEmpty()
        planBridge.updateSelectedCourses(selectedCourses, canEditPlan = planUiState is CoursePlanUiState.Success)
    }

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.toggleRequests.collect { course ->
            planViewModel.toggleCourse(course)
        }
    }

    CoursePlanningTimetableView(
        uiState = planUiState,
        modifier = Modifier.fillMaxSize(),
    )
}
