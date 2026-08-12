package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.presentation.viewModel.CourseSelectionViewModel
import org.mpc.presentation.views.courseSelection.CourseSelectionTimetableView

@Composable
fun CourseSelectionTimetableViewBinding(planBridge: CoursePlanBridge) {
    val planViewModel: CourseSelectionViewModel = metroViewModel()

    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.saveRequests.collect {
            planViewModel.savePlan()
        }
    }

    CourseSelectionTimetableView(
        uiState = planUiState,
        modifier = Modifier.fillMaxSize(),
    )
}
