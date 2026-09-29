package org.mpc.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    selectedView: CoursePlanningView,
    onSelectedViewChange: (CoursePlanningView) -> Unit,
    onSelectedCoursesChange: (List<CourseSummary>) -> Unit = {},
    onCourseClick: (semester: String, course: CourseSummary) -> Unit = { _, _ -> },
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    val stateHolder = rememberSaveableStateHolder()
    val selectedCourses =
        (planUiState as? CoursePlanUiState.Success)
            ?.plan
            ?.selectedCourses
            ?.values
            ?.sortedBy { course -> course.title }
            .orEmpty()
    SideEffect {
        onSelectedCoursesChange(selectedCourses)
    }
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
            state =
            CompactCoursePlanningState(
                selectedView = selectedView,
                onSelectedViewChange = onSelectedViewChange,
                planViewModel = planViewModel,
                planUiState = planUiState,
                onCourseClick = onCourseClick,
            ),
        )
    }
}

private data class CompactCoursePlanningState(
    val selectedView: CoursePlanningView,
    val onSelectedViewChange: (CoursePlanningView) -> Unit,
    val planViewModel: CoursePlanViewModel,
    val planUiState: CoursePlanUiState,
    val onCourseClick: (semester: String, course: CourseSummary) -> Unit,
)

@Composable
private fun CompactCoursePlanningScreen(
    modifier: Modifier,
    stateHolder: SaveableStateHolder,
    state: CompactCoursePlanningState,
) {
    Column(modifier = modifier) {
        ButtonGroup(
            overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            CoursePlanningView.entries.forEach { view ->
                toggleableItem(
                    checked = state.selectedView == view,
                    onCheckedChange = { isChecked ->
                        if (isChecked) state.onSelectedViewChange(view)
                    },
                    label = view.label,
                    icon = {
                        Icon(
                            imageVector = view.icon,
                            contentDescription = null,
                        )
                    },
                    weight = 1f,
                )
            }
        }
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            stateHolder.SaveableStateProvider(state.selectedView) {
                when (state.selectedView) {
                    CoursePlanningView.CATALOG -> {
                        CourseCatalogScreen(
                            modifier = Modifier.fillMaxSize(),
                            onCourseClick = state.onCourseClick,
                            planViewModel = state.planViewModel,
                        )
                    }

                    CoursePlanningView.TIMETABLE -> {
                        CoursePlanningTimetableView(
                            uiState = state.planUiState,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

enum class CoursePlanningView(
    val label: String,
    val title: String,
    val icon: ImageVector,
) {
    CATALOG("搜尋", "課程搜尋", Icons.Default.Search),
    TIMETABLE("課表", "我的課表", Icons.Default.DateRange),
}

private const val CATALOG_WEIGHT = 0.42f
private const val TIMETABLE_WEIGHT = 0.58f
