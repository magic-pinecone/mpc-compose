package org.mpc.presentation.views.courseSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.model.entity.CourseSerialNo
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.presentation.views.courseSelection.components.CourseCard

@Composable
fun CourseResultSuccessView(
    modifier: Modifier,
    courseResult: CourseResult,
    selectedCourseSerialNumbers: Set<CourseSerialNo>,
    onCourseToggle: (CourseSummary) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = courseResult.courses,
            key = { course -> course.serialNo }
        ) { course ->
            CourseCard(
                modifier = Modifier.fillMaxWidth(),
                courseSummary = course,
                isSelected = course.serialNo in selectedCourseSerialNumbers,
                onButtonClick = { onCourseToggle(course) },
            )
        }
    }
}
