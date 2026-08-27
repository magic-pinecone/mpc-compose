package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CancellationException
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.repository.CourseRepository
import org.mpc.navigation.CourseDetailsRoute
import org.mpc.presentation.state.CoursePlanUiState
import org.mpc.presentation.viewModel.CoursePlanViewModel
import org.mpc.presentation.views.courseDetails.CourseDetailsActions
import org.mpc.presentation.views.courseDetails.CourseDetailsUiState
import org.mpc.presentation.views.courseDetails.CourseDetailsView

@Composable
fun CourseDetailsScreen(
    route: CourseDetailsRoute,
    courseRepository: CourseRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    planViewModel: CoursePlanViewModel = metroViewModel(),
) {
    var loadState by remember(route) { mutableStateOf<CourseDetailsLoadState>(CourseDetailsLoadState.Loading) }
    var retryRequest by remember(route) { mutableIntStateOf(0) }
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(route, retryRequest) {
        var summary = (loadState as? CourseDetailsLoadState.Content)?.summary
        if (summary == null) {
            loadState = CourseDetailsLoadState.Loading
            summary =
                try {
                    courseRepository
                        .fetchCoursesBySerialNo(
                            semester = route.semester,
                            serialNos = listOf(CourseSerialNo(route.serialNumber)),
                        ).courses
                        .singleOrNull()
                } catch (cause: CancellationException) {
                    throw cause
                } catch (_: Exception) {
                    null
                }

            if (summary == null) {
                loadState = CourseDetailsLoadState.Unavailable
                return@LaunchedEffect
            }
        }

        loadState = CourseDetailsLoadState.Content(summary = summary)
        loadState =
            try {
                CourseDetailsLoadState.Content(
                    summary = summary,
                    detail =
                    courseRepository.fetchCourseDetail(
                        semester = route.semester,
                        serialNo = route.serialNumber,
                    ),
                    isLoading = false,
                )
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Exception) {
                CourseDetailsLoadState.Content(
                    summary = summary,
                    isLoading = false,
                    supplementalLoadFailed = true,
                )
            }
    }

    val content = loadState as? CourseDetailsLoadState.Content
    val isSelected =
        content?.summary?.let { summary ->
            (planUiState as? CoursePlanUiState.Success)
                ?.plan
                ?.selectedCourses
                ?.containsKey(summary.serialNo)
        } ?: false

    CourseDetailsView(
        state =
        CourseDetailsUiState(
            summary = content?.summary,
            detail = content?.detail,
            isLoading = loadState is CourseDetailsLoadState.Loading || content?.isLoading == true,
            errorMessage =
            when {
                loadState is CourseDetailsLoadState.Unavailable -> "無法載入這門課程。"
                content?.supplementalLoadFailed == true -> "無法載入完整課程資訊。"
                else -> null
            },
            isSelected = isSelected,
        ),
        actions =
        CourseDetailsActions(
            onToggleCourse = {
                content?.summary?.let(planViewModel::toggleCourse)
            },
            onRetry = { retryRequest++ },
            onClose = onClose,
        ),
        modifier =
        modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .heightIn(max = 720.dp),
    )
}

private sealed interface CourseDetailsLoadState {
    data object Loading : CourseDetailsLoadState

    data object Unavailable : CourseDetailsLoadState

    data class Content(
        val summary: CourseSummary,
        val detail: CourseDetail? = null,
        val isLoading: Boolean = true,
        val supplementalLoadFailed: Boolean = false,
    ) : CourseDetailsLoadState
}
