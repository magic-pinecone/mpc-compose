package org.mpc.presentation.views.courseSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import org.mpc.presentation.model.toTimetableBlocks


@Composable
fun CourseSelectionTimetableSuccessView(
    snapshot: CoursePlanSnapshot,
    displayWeekends: Boolean,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = snapshot.selected.values.flatMap { it.toTimetableBlocks() },
                key = { course -> course.serialNo.value }
            ) { items ->
                Text("${items.title}：${items.type.description}")
            }

        }


    }
}