package org.mpc.presentation.views.coursePlanning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.CourseResult
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.presentation.views.coursePlanning.components.CourseCard

@Composable
fun CourseSearchResultSuccessView(
    modifier: Modifier,
    courseResult: CourseResult,
    selectedCourseSerialNumbers: Set<CourseSerialNo>,
    onCourseClick: (CourseSummary) -> Unit,
    onCourseToggle: (CourseSummary) -> Unit,
    isCompact: Boolean = false,
    canEditPlan: Boolean = true,
) {
    if (courseResult.courses.isEmpty()) {
        Box(
            modifier = modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("找不到符合的課程", style = MaterialTheme.typography.titleMedium)
                Text(
                    "試試其他關鍵字，或清除搜尋條件查看全部課程。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "result-count") {
            Text(
                text = "共 ${courseResult.courses.size} 門課程",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(
            items = courseResult.courses,
            key = { course -> course.serialNo.value },
        ) { course ->
            CourseCard(
                modifier = Modifier.fillMaxWidth(),
                courseSummary = course,
                isSelected = course.serialNo in selectedCourseSerialNumbers,
                isCompact = isCompact,
                canEditPlan = canEditPlan,
                onCardClick = { onCourseClick(course) },
                onButtonClick = { onCourseToggle(course) },
            )
        }
    }
}
