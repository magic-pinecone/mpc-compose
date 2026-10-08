package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CourseCatalogSnapshot
import org.mpc.bridge.CoursePlanBridge
import org.mpc.bridge.CourseSearchBridge
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.state.CourseSearchError
import org.mpc.presentation.state.CourseSearchResultUiState
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

    val selectedCourses =
        (planUiState as? CoursePlanUiState.Success)
            ?.plan
            ?.selectedCourses
            ?.values
            ?.sortedBy { course -> course.title }
            .orEmpty()
    val catalogSnapshot =
        when (val result = searchUiState.result) {
            CourseSearchResultUiState.Loading -> CourseCatalogSnapshot(
                semester = searchUiState.semester,
                isLoading = true,
            )

            is CourseSearchResultUiState.Success -> CourseCatalogSnapshot(
                semester = searchUiState.semester,
                courses = result.result.courses,
                isLoading = false,
            )

            is CourseSearchResultUiState.Failure -> CourseCatalogSnapshot(
                semester = searchUiState.semester,
                isLoading = false,
                failureMessage = when (result.error) {
                    CourseSearchError.NETWORK -> "目前無法連線，請確認網路狀態後再試一次。"
                    CourseSearchError.INVALID_SEMESTER -> "目前不支援這個學期的課程查詢。"
                    CourseSearchError.UNKNOWN -> "課程資料載入失敗，請稍後再試。"
                },
            )
        }

    SideEffect {
        planBridge.updateSelectedCourses(selectedCourses, canEditPlan = planUiState is CoursePlanUiState.Success)
        bridge.updateCatalogSnapshot(catalogSnapshot)
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

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.toggleRequests.collect { course ->
            planViewModel.toggleCourse(course)
        }
    }

    CourseSearchResultView(
        modifier = Modifier.fillMaxSize(),
        uiState = searchUiState.result,
        selectedCourseSerialNumbers = selectedCourses.map { it.serialNo }.toSet(),
        onCourseClick = { course ->
            planBridge.requestCourseDetails(semester = searchUiState.semester, course = course)
        },
        onToggleCourse = planViewModel::toggleCourse,
    )
}
