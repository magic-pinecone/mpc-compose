package org.mpc.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.model.CourseSummary
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.coursePlanning.CoursePlanningTimetableView

@Composable
fun CoursePlanningScreen(
    modifier: Modifier = Modifier,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit = { _, _ -> },
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    val stateHolder = rememberSaveableStateHolder()
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (isExpanded) {
        Row(modifier = modifier) {
            stateHolder.SaveableStateProvider(CoursePlanningView.CATALOG) {
                CourseCatalogScreen(
                    modifier =
                    Modifier
                        .weight(CATALOG_WEIGHT)
                        .fillMaxHeight(),
                    planViewModel = planViewModel,
                    onCourseClick = onCourseClick,
                )
            }
            VerticalDivider()
            stateHolder.SaveableStateProvider(CoursePlanningView.TIMETABLE) {
                CoursePlanningTimetableView(
                    uiState = planUiState,
                    modifier =
                    Modifier
                        .weight(TIMETABLE_WEIGHT)
                        .fillMaxHeight(),
                )
            }
        }
    } else {
        CompactCoursePlanningScreen(
            modifier = modifier,
            stateHolder = stateHolder,
            planViewModel = planViewModel,
            planUiState = planUiState,
            onCourseClick = onCourseClick,
        )
    }
}

@Composable
private fun CompactCoursePlanningScreen(
    modifier: Modifier,
    stateHolder: SaveableStateHolder,
    planViewModel: CoursePlanViewModel,
    planUiState: CoursePlanUiState,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit,
) {
    var selectedView by rememberSaveable { mutableStateOf(CoursePlanningView.CATALOG) }

    Column(modifier = modifier) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CoursePlanningView.entries.forEach { view ->
                FilterChip(
                    selected = selectedView == view,
                    onClick = { selectedView = view },
                    label = { Text(view.label) },
                )
            }
        }
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            stateHolder.SaveableStateProvider(selectedView) {
                when (selectedView) {
                    CoursePlanningView.CATALOG -> {
                        CourseCatalogScreen(
                            modifier = Modifier.fillMaxSize(),
                            onCourseClick = onCourseClick,
                            planViewModel = planViewModel,
                        )
                    }

                    CoursePlanningView.TIMETABLE -> {
                        CoursePlanningTimetableView(
                            uiState = planUiState,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

private enum class CoursePlanningView(
    val label: String,
) {
    CATALOG("課程查詢"),
    TIMETABLE("課表"),
}

private const val CATALOG_WEIGHT = 0.42f
private const val TIMETABLE_WEIGHT = 0.58f
