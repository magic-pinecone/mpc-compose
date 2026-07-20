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
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.viewModel.CourseSelectionViewModel
import org.mpc.presentation.views.courseSelection.CourseSearchResultView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionSearchScreen(
    modifier: Modifier,
    searchViewModel: CourseSearchViewModel = metroViewModel(),
    planViewModel: CourseSelectionViewModel = metroViewModel(),
) {
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val planState by planViewModel.state.collectAsStateWithLifecycle()

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                    query -> searchViewModel.updateQuery(
                semester = searchState.semester,
                query = query
            )
                searchViewModel.onSearch()
            },
            placeholder = {
                Text("搜尋課程")
            }
        )
    }

    Column(
        modifier = modifier
    ) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp,
                    vertical = 8.dp
                ),
            state = searchBarState,
            inputField = inputField
        )

        CourseSearchResultView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            courseLoadState = searchState.result,
            selectedCourseSerialNumbers = planState.selected.keys,
            onToggleCourse = planViewModel::toggleCourse,
        )
    }
    ExpandedFullScreenSearchBar(
        modifier = modifier,
        state = searchBarState,
        inputField = inputField,
    ) {
        CourseSearchResultView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            courseLoadState = searchState.result,
            selectedCourseSerialNumbers = planState.selected.keys,
            onToggleCourse = planViewModel::toggleCourse,
        )
    }

}
