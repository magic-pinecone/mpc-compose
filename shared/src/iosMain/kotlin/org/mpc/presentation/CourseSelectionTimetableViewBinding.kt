package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.presentation.viewModel.CourseSelectionViewModel
import org.mpc.presentation.views.courseSelection.CourseSelectionTimetableView

@Composable
fun CourseSelectionTimetableViewBinding(){
    val planViewModel: CourseSelectionViewModel = metroViewModel()

    val planState by planViewModel.state.collectAsStateWithLifecycle()

    CourseSelectionTimetableView(
        state = planState,
        displayWeekends = false,
        modifier = Modifier.fillMaxSize()
    )
}