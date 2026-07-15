package org.mpc.presentationn.views.courseSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.model.state.CourseLoadState
import org.mpc.domain.model.state.CourseSearchError
import org.mpc.domain.viewModel.CourseSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSearchResultView(
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

    Column (
        modifier = modifier,
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
        CourseResultView(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            courseLoadState = uiState.result)
    }

    ExpandedFullScreenSearchBar(
        modifier = modifier,
        state = searchBarState,
        inputField = inputField,
    ) {
        CourseResultView(
            modifier = modifier,
            courseLoadState = uiState.result)
    }


}

@Composable
fun CourseResultView(modifier: Modifier, courseLoadState: CourseLoadState) {
    when(courseLoadState) {
        is CourseLoadState.Success -> CourseResultSuccessView(modifier, courseLoadState.courses)
        is CourseLoadState.Failure -> CourseResultFailureView(modifier, courseLoadState.error)
        CourseLoadState.Loading -> CourseResultLoadingView(modifier)
    }
}

@Composable
fun CourseResultSuccessView(modifier: Modifier, courseResult: CourseResult) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = courseResult.courses,
            key = { course -> course.serialNo }
        ) {
            course -> CourseCard(
                courseSummary = course,
                modifier = Modifier.fillMaxWidth(),
                onJoinClick = {}
            )

        }
    }

}

@Composable
fun CourseResultFailureView(modifier: Modifier, error: CourseSearchError) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Failed to load: $error")
    }
}

@Composable
fun CourseResultLoadingView(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...")
    }
}