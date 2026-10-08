package org.mpc.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.model.CourseSummary
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.views.coursePlanning.CourseSearchResultView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCatalogScreen(
    modifier: Modifier,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit = { _, _ -> },
    isCompact: Boolean = false,
    searchViewModel: CourseSearchViewModel = metroViewModel(),
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf(searchUiState.query) }
    val keyboard = LocalSoftwareKeyboardController.current
    val submitSearch = {
        searchViewModel.updateQuery(semester = searchUiState.semester, query = query)
        searchViewModel.onSearch()
        keyboard?.hide()
    }
    val selectedCourseSerialNumbers =
        (planUiState as? CoursePlanUiState.Success)?.plan?.selectedCourses?.keys.orEmpty()

    val searchContainerColor = SearchBarDefaults.colors().containerColor

    Column(modifier = modifier) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = { value ->
                query = value
                if (value.isEmpty()) submitSearch()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜尋課程") },
            colors = SearchBarDefaults.inputFieldColors(
                focusedContainerColor = searchContainerColor,
                unfocusedContainerColor = searchContainerColor,
            ),
            leadingIcon = {
                IconButton(onClick = { submitSearch() }) {
                    Icon(Icons.Default.Search, contentDescription = "搜尋課程")
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        submitSearch()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "清除搜尋")
                    }
                }
            },
            onSearch = { submitSearch() },
            // Results stay in the catalog pane instead of opening an expanded search surface.
            expanded = false,
            onExpandedChange = {},
        )
        CourseSearchResultView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            uiState = searchUiState.result,
            selectedCourseSerialNumbers = selectedCourseSerialNumbers,
            onCourseClick = { course -> onCourseClick(searchUiState.semester, course) },
            onToggleCourse = planViewModel::toggleCourse,
            isCompact = isCompact,
            canEditPlan = planUiState is CoursePlanUiState.Success,
            onRetry = { submitSearch() },
        )
    }
}
