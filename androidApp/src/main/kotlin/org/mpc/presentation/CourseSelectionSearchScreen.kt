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
import org.mpc.domain.viewModel.CourseSearchViewModel
import org.mpc.presentation.views.courseSelection.CourseSearchResultView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionSearchScreen(
    modifier: Modifier,
    viewModel: CourseSearchViewModel = metroViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                    query -> viewModel.updateQuery(
                semester = uiState.semester,
                query = query
            )
                viewModel.onSearch()
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
            courseLoadState = uiState.result
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
            courseLoadState = uiState.result
        )
    }

}