package org.mpc.presentation.views.courseDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseSummary

@Composable
fun CourseDetailsView(
    state: CourseDetailsUiState,
    actions: CourseDetailsActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.summary?.title ?: "課程詳細資訊",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    state.summary?.let { course ->
                        Text(
                            text = "${course.classNo} · ${course.serialNo.value}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = actions.onClose) {
                    Text("關閉")
                }
            }

            state.summary?.let {
                Button(onClick = actions.onToggleCourse) {
                    Text(if (state.isSelected) "從課表移除" else "加入課表")
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.errorMessage?.let { message ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = actions.onRetry) {
                        Text("重試")
                    }
                }
            }

            state.detail?.let { courseDetail ->
                HorizontalDivider()
                DetailSection("課程目標", courseDetail.objectives)
                DetailSection("課程內容", courseDetail.content)
                DetailSection("教科書", courseDetail.books)
                DetailSection("教學方式", courseDetail.teachingMethod)
                DetailSection("評量方式", courseDetail.gradingPolicy)
                if (courseDetail.distributionConditions.isNotEmpty()) {
                    DetailSection(
                        title = "分發條件",
                        content =
                        courseDetail.distributionConditions
                            .sortedBy { condition -> condition.priority }
                            .joinToString(separator = "\n") { condition -> condition.rule },
                    )
                }
            }
        }
    }
}

data class CourseDetailsUiState(
    val summary: CourseSummary?,
    val detail: CourseDetail?,
    val isLoading: Boolean,
    val errorMessage: String?,
    val isSelected: Boolean,
)

data class CourseDetailsActions(
    val onToggleCourse: () -> Unit,
    val onRetry: () -> Unit,
    val onClose: () -> Unit,
)

@Composable
private fun DetailSection(
    title: String,
    content: String,
) {
    if (content.isBlank()) {
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
