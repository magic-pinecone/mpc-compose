package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CourseSearchBridge
import org.mpc.presentation.viewModel.CourseSearchViewModel
import org.mpc.presentation.views.courseSelection.CourseSearchResultView

@Composable
fun CourseSearchResultViewBinding(
    bridge: CourseSearchBridge
) {
    val viewModel: CourseSearchViewModel = metroViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(bridge, viewModel) {
        bridge.sendRequests.collect { (semester, query) ->
            viewModel.updateQuery(semester, query)
            viewModel.onSearch()
        }
    }

    CourseSearchResultView(
        modifier = Modifier.fillMaxSize(),
        courseLoadState = uiState.result,
    )

}