package org.mpc.presentation.views.coursePlanning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CoursePlan
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.PasswordCardType
import org.mpc.presentation.model.CourseTimetableBlock
import org.mpc.presentation.model.toTimetableBlocks

@Composable
fun CoursePlanningTimetableSuccessView(
    plan: CoursePlan,
    modifier: Modifier,
) {
    val columns = 5
    val rows = CoursePeriod.entries.size
    val timetableBlocks =
        plan
            .toTimetableBlocks()
            .filter { it.time.day.order <= columns }

    Box(
        modifier =
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row {
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                ) {
                    CourseDay.entries
                        .take(columns)
                        .forEach {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                            ) {
                                Text(
                                    text = it.description,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(),
                ) {
                    CoursePeriod.entries
                        .forEach {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            ) {
                                Text(
                                    text = it.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                }
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val columnWidth = maxWidth / columns
                    val rowHeight = maxHeight / rows

                    TimetableBackground(
                        columns = columns,
                        rows = rows,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        repeat(columns * rows) {
                            TimetableBackgroundCell()
                        }
                    }

                    timetableBlocks.forEach { block ->
                        TimetableForegroundCell(
                            block = block,
                            modifier =
                            Modifier
                                .offset(
                                    x = columnWidth * (block.time.day.order - 1),
                                    y = rowHeight * (block.time.period.order - 1),
                                ).width(columnWidth)
                                .height(rowHeight * block.span),
                        )
                    }

                    if (plan.selectedCourses.isEmpty()) {
                        Text(
                            text = "尚未加入課程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableBackground(
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val cellWidth = constraints.maxWidth / columns
        val cellHeight = constraints.maxHeight / rows

        val cellConstraints = Constraints.fixed(
            width = cellWidth,
            height = cellHeight,
        )

        val placeable = measurables.map { measurable ->
            measurable.measure(cellConstraints)
        }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            placeable.forEachIndexed { index, placeable ->
                val column = index % columns
                val row = index / columns

                placeable.placeRelative(
                    x = column * cellWidth,
                    y = row * cellHeight,
                )
            }
        }
    }
}

@Composable
fun TimetableBackgroundCell() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun TimetableForegroundCell(
    block: CourseTimetableBlock,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        when (block.type) {
            CourseType.REQUIRED -> MaterialTheme.colorScheme.primaryContainer
            CourseType.ELECTIVE -> MaterialTheme.colorScheme.secondaryContainer
            CourseType.UNKNOWN -> MaterialTheme.colorScheme.tertiaryContainer
        }
    val contentColor =
        when (block.type) {
            CourseType.REQUIRED -> MaterialTheme.colorScheme.onPrimaryContainer
            CourseType.ELECTIVE -> MaterialTheme.colorScheme.onSecondaryContainer
            CourseType.UNKNOWN -> MaterialTheme.colorScheme.onTertiaryContainer
        }

    Surface(
        modifier =
        modifier
            .padding(2.dp),
        shape = RoundedCornerShape(4.dp),
        color = containerColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
            Modifier
                .fillMaxSize()
                .padding(2.dp),
        ) {
            Text(
                text = block.title,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                maxLines = block.span.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
internal fun PreviewCoursePlanningTimetableSuccessView(
    plan: CoursePlan =
        CoursePlan(
            semester = "115-1",
            selectedCourses =
            mapOf(
                CourseSerialNo("36019")
                    to
                    CourseSummary(
                        serialNo = CourseSerialNo("36019"),
                        classNo = "ENA103-*",
                        title = "專題討論（III）",
                        credit = 0.0,
                        passwordCard = PasswordCardType.NONE,
                        teachers = listOf("鄭明敏", "林居慶", "林進榮", "林伯勳"),
                        classTimes =
                        listOf(
                            CourseTime(CourseDay.FRIDAY, CoursePeriod.A),
                            CourseTime(CourseDay.FRIDAY, CoursePeriod.B),
                            CourseTime(CourseDay.FRIDAY, CoursePeriod.C),
                        ),
                        limitCnt = 0,
                        admitCnt = 0,
                        waitCnt = 0,
                        collegeName = "工學院",
                        departmentName = "環境工程研究所碩士班",
                        courseType = CourseType.ELECTIVE,
                        detailUrl = "https://cis.ncu.edu.tw/Course/main/support/courseDetail.html?crs=36019",
                    ),
            ),
        ),
) {
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("我的課表")
                    },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            CoursePlanningTimetableSuccessView(
                plan = plan,
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}
