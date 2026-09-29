package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.courseDetails.CourseDetailsContent
import org.mpc.presentation.views.coursePlanning.CoursePlanningTimetableView

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CoursePlanningTimetableViewBinding(
    planBridge: CoursePlanBridge,
    courseRepository: CourseRepository,
) {
    val planViewModel: CoursePlanViewModel = metroViewModel()

    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCourse by remember { mutableStateOf<SelectedCourseDetails?>(null) }
    val sheetState = rememberAdaptiveSheetState()
    val selectedCourses =
        (planUiState as? CoursePlanUiState.Success)
            ?.plan
            ?.selectedCourses
            ?.values
            ?.sortedBy { course -> course.title }
            .orEmpty()

    SideEffect {
        planBridge.updateSelectedCourses(selectedCourses)
    }

    LaunchedEffect(planBridge, planViewModel) {
        planBridge.saveRequests.collect {
            planViewModel.savePlan()
        }
    }

    CoursePlanningTimetableView(
        uiState = planUiState,
        modifier = Modifier.fillMaxSize(),
        onCourseClick = { semester, course ->
            selectedCourse = SelectedCourseDetails(semester, course)
        },
    )

    selectedCourse?.let { selection ->
        AdaptiveBottomSheet(
            onDismissRequest = { selectedCourse = null },
            adaptiveSheetState = sheetState,
        ) {
            CourseDetailsContent(
                semester = selection.semester,
                serialNumber = selection.course.serialNo.value,
                courseRepository = courseRepository,
                onClose = { selectedCourse = null },
                modifier = Modifier.fillMaxWidth(),
                initialSummary = selection.course,
                planViewModel = planViewModel,
            )
        }
    }
}

private data class SelectedCourseDetails(
    val semester: String,
    val course: CourseSummary,
)
