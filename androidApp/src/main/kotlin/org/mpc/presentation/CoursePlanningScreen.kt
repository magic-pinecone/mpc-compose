package org.mpc.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AdaptStrategy
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.courseDetails.CourseDetailsBottomSheet
import org.mpc.presentation.views.courseDetails.CourseDetailsSelection
import org.mpc.presentation.views.coursePlanning.CoursePlanningTimetableView
import org.mpc.presentation.views.coursePlanning.SelectedCoursesBottomSheet

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CoursePlanningScreen(
    modifier: Modifier = Modifier,
    selectedView: CoursePlanningView,
    onSelectedViewChange: (CoursePlanningView) -> Unit,
    courseRepository: CourseRepository,
    onSelectedCoursesChange: (List<CourseSummary>) -> Unit = {},
    isShowingSelectedCourses: Boolean = false,
    onDismissSelectedCourses: () -> Unit = {},
    isCompactCatalog: Boolean = false,
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
    var selectedCourseDetails by remember { mutableStateOf<CourseDetailsSelection?>(null) }
    val onCourseClick: (semester: String, course: CourseSummary) -> Unit = { semester, course ->
        selectedCourseDetails = CourseDetailsSelection(semester, course)
    }

    SideEffect {
        onSelectedCoursesChange(selectedCourses)
    }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isTabletop = adaptiveInfo.windowPosture.isTabletop
    val directive = calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(adaptiveInfo).let {
        it.copy(
            maxHorizontalPartitions = if (isTabletop) 1 else it.maxHorizontalPartitions.coerceAtMost(2),
            maxVerticalPartitions = if (isTabletop) 2 else 1,
        )
    }
    val currentPane = if (isTabletop || selectedView == CoursePlanningView.TIMETABLE) {
        SupportingPaneScaffoldRole.Supporting
    } else {
        SupportingPaneScaffoldRole.Main
    }
    val scaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        maxVerticalPartitions = directive.maxVerticalPartitions,
        adaptStrategies = SupportingPaneScaffoldDefaults.adaptStrategies(
            mainPaneAdaptStrategy = if (isTabletop) {
                AdaptStrategy.Reflow(SupportingPaneScaffoldRole.Supporting)
            } else {
                AdaptStrategy.Hide
            },
            supportingPaneAdaptStrategy = AdaptStrategy.Hide,
        ),
        currentDestination = ThreePaneScaffoldDestinationItem<Unit>(currentPane),
    )
    val showsBothPanes = scaffoldValue.primary != PaneAdaptedValue.Hidden &&
        scaffoldValue.secondary != PaneAdaptedValue.Hidden

    Column(modifier = modifier) {
        if (!showsBothPanes) {
            CoursePlanningSectionPicker(selectedView, onSelectedViewChange)
        }
        SupportingPaneScaffold(
            modifier = Modifier.fillMaxWidth().weight(1f),
            directive = directive,
            value = scaffoldValue,
            mainPane = {
                AnimatedPane {
                    CourseCatalogPane(
                        modifier = Modifier.fillMaxSize(),
                        stateHolder = stateHolder,
                        isCompactCatalog = isCompactCatalog,
                        planViewModel = planViewModel,
                        onCourseClick = onCourseClick,
                    )
                }
            },
            supportingPane = {
                AnimatedPane {
                    CourseTimetablePane(
                        modifier = Modifier.fillMaxSize(),
                        stateHolder = stateHolder,
                        planUiState = planUiState,
                        onCourseClick = onCourseClick,
                    )
                }
            },
        )
    }

    selectedCourseDetails?.let { selection ->
        CourseDetailsBottomSheet(
            selection = selection,
            courseRepository = courseRepository,
            onDismissRequest = { selectedCourseDetails = null },
            planViewModel = planViewModel,
        )
    }

    if (isShowingSelectedCourses) {
        SelectedCoursesBottomSheet(
            courses = selectedCourses,
            onDismissRequest = onDismissSelectedCourses,
            onRemoveCourse = planViewModel::toggleCourse,
            canEditPlan = planUiState is CoursePlanUiState.Success,
        )
    }
}

@Composable
private fun CourseCatalogPane(
    modifier: Modifier,
    stateHolder: SaveableStateHolder,
    isCompactCatalog: Boolean,
    planViewModel: CoursePlanViewModel,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit,
) {
    stateHolder.SaveableStateProvider(CoursePlanningView.CATALOG) {
        CourseCatalogScreen(
            modifier = modifier,
            isCompact = isCompactCatalog,
            planViewModel = planViewModel,
            onCourseClick = onCourseClick,
        )
    }
}

@Composable
private fun CourseTimetablePane(
    modifier: Modifier,
    stateHolder: SaveableStateHolder,
    planUiState: CoursePlanUiState,
    onCourseClick: (semester: String, course: CourseSummary) -> Unit,
) {
    stateHolder.SaveableStateProvider(CoursePlanningView.TIMETABLE) {
        CoursePlanningTimetableView(
            uiState = planUiState,
            onCourseClick = onCourseClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun CoursePlanningSectionPicker(
    selectedView: CoursePlanningView,
    onSelectedViewChange: (CoursePlanningView) -> Unit,
) {
    ButtonGroup(
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        CoursePlanningView.entries.forEach { view ->
            toggleableItem(
                checked = selectedView == view,
                onCheckedChange = { isChecked ->
                    if (isChecked) onSelectedViewChange(view)
                },
                label = view.label,
                icon = { Icon(imageVector = view.icon, contentDescription = null) },
                weight = 1f,
            )
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
