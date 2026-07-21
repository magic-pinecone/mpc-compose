package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CourseSearchBridge
import org.mpc.domain.model.state.CoursePlanState
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.viewModel.CourseSelectionViewModel
import org.mpc.presentation.views.courseSelection.CourseSearchResultView

@Composable
fun CourseSearchResultViewBinding(
    bridge: CourseSearchBridge
) {
    val searchViewModel: CourseSearchViewModel = metroViewModel()
    val planViewModel: CourseSelectionViewModel = metroViewModel()

    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val planState by planViewModel.state.collectAsStateWithLifecycle()

    val selectedCourseSerialNumbers = when (val current = planState) {
        CoursePlanState.Loading -> emptySet()
        is CoursePlanState.Failure -> emptySet()
        is CoursePlanState.Success -> current.snapshot.selected.keys
    }

    LaunchedEffect(bridge, searchViewModel) {
        bridge.sendRequests.collect { (semester, query) ->
            searchViewModel.updateQuery(semester, query)
            searchViewModel.onSearch()
        }
    }

    CourseSearchResultView(
        modifier = Modifier.fillMaxSize(),
        courseLoadState = searchState.result,
        selectedCourseSerialNumbers = selectedCourseSerialNumbers,
        onToggleCourse = planViewModel::toggleCourse,
    )
}
