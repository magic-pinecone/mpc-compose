package org.mpc.presentation.views.courseDetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.viewModel.CoursePlanViewModel

data class CourseDetailsSelection(
    val semester: String,
    val course: CourseSummary,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CourseDetailsBottomSheet(
    selection: CourseDetailsSelection,
    courseRepository: CourseRepository,
    onDismissRequest: () -> Unit,
    planViewModel: CoursePlanViewModel,
) {
    val sheetState = rememberAdaptiveSheetState()

    AdaptiveBottomSheet(
        onDismissRequest = onDismissRequest,
        adaptiveSheetState = sheetState,
    ) {
        CourseDetailsContent(
            semester = selection.semester,
            serialNumber = selection.course.serialNo.value,
            courseRepository = courseRepository,
            onClose = onDismissRequest,
            modifier = Modifier.fillMaxWidth(),
            initialSummary = selection.course,
            planViewModel = planViewModel,
        )
    }
}
