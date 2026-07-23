package org.mpc.presentation.views.courseSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CoursePlan
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.PasswordCardType

@Composable
fun CourseSelectionTimetableSuccessView(
    plan: CoursePlan,
    modifier: Modifier,
) {
    val columns = 5
    val rows = CoursePeriod.entries.size

    Logger.i(plan.toString())


    Box(modifier = modifier
        .padding(16.dp, 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    CourseDay.entries
                        .take(columns)
                        .forEach {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = it.description,
                                )
                            }
                        }

                }
            }

            Spacer(Modifier.height(4.dp))

            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                ) {
                    CoursePeriod.entries
                        .forEach {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = it.description,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TimetableGrid(
                        columns = columns,
                        rows = rows,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        repeat(columns * rows) {
                            TimetableCell(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            )

                        }
                    }

                }
            }
        }
    }
}

@Composable
fun TimetableGrid(
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
fun TimetableCell(
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
internal fun PreviewCourseSelectionTimetableSuccessView(
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
                    }
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            CourseSelectionTimetableSuccessView(
                plan = plan,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            )
        }
    }
}
