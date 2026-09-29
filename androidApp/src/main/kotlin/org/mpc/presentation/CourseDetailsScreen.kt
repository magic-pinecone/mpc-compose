package org.mpc.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.repository.CourseRepository
import org.mpc.navigation.CourseDetailsRoute
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.courseDetails.CourseDetailsContent

@Composable
fun CourseDetailsScreen(
    route: CourseDetailsRoute,
    courseRepository: CourseRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    CourseDetailsContent(
        semester = route.semester,
        serialNumber = route.serialNumber,
        courseRepository = courseRepository,
        onClose = onClose,
        modifier = modifier,
        planViewModel = planViewModel,
    )
}
