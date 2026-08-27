package org.mpc.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.model.CourseSummary
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.coursePlanning.CourseSearchResultView
import kotlin.collections.emptySet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCatalogScreen(
    modifier: Modifier,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit = { _, _ -> },
    searchViewModel: CourseSearchViewModel = metroViewModel(),
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = { query ->
                searchViewModel.updateQuery(
                    semester = searchUiState.semester,
                    query = query,
                )
                searchViewModel.onSearch()
            },
            placeholder = {
                Text("搜尋課程")
            },
        )
    }

    val selectedCourseSerialNumbers =
        when (val current = planUiState) {
            CoursePlanUiState.Loading -> emptySet()
            is CoursePlanUiState.Failure -> emptySet()
            is CoursePlanUiState.Success -> current.plan.selectedCourses.keys
        }

    Column(
        modifier = modifier,
    ) {
        SearchBar(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            state = searchBarState,
            inputField = inputField,
        )

        CourseSearchResultView(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
            uiState = searchUiState.result,
            selectedCourseSerialNumbers = selectedCourseSerialNumbers,
            onCourseClick = { course -> onCourseClick(searchUiState.semester, course) },
            onToggleCourse = planViewModel::toggleCourse,
        )
    }
    ExpandedFullScreenSearchBar(
        modifier = modifier,
        state = searchBarState,
        inputField = inputField,
    ) {
        CourseSearchResultView(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
            uiState = searchUiState.result,
            selectedCourseSerialNumbers = selectedCourseSerialNumbers,
            onCourseClick = { course -> onCourseClick(searchUiState.semester, course) },
            onToggleCourse = planViewModel::toggleCourse,
        )
    }
}
