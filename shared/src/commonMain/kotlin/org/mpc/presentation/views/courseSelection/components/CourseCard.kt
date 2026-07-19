package org.mpc.presentation.views.courseSelection.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.entity.CourseType
import org.mpc.domain.model.entity.PasswordCardType
import org.mpc.presentation.icon.apartment
import org.mpc.presentation.icon.groups
import org.mpc.presentation.icon.key
import org.mpc.presentation.icon.schedule

@Composable
fun CourseCard(
    modifier: Modifier = Modifier,
    courseSummary: CourseSummary,
    isSelected: Boolean,
    onButtonClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = courseSummary.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                CourseTypeBadge(courseSummary.courseType)
            }

            Text(
                text = courseSummary.informationText(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )

            CourseInfoRail(courseSummary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = courseSummary.scheduleText(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.heightIn(min = 40.dp),
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Text(if (isSelected) "移除" else "加入")
                }
            }
        }
    }
}

@Composable
private fun CourseTypeBadge(courseType: CourseType) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = courseType.description,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium,
        )
    }
}
@Composable
private fun CourseInfoRail(courseSummary: CourseSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CourseInfoBadge(
            imageVector = apartment,
            infoText = courseSummary.departmentText(),
        )
        CourseInfoBadge(
            imageVector = groups,
            infoText = courseSummary.enrollmentText(),
        )
        CourseInfoBadge(
            imageVector = key,
            infoText = courseSummary.passwordText(),
        )
    }
}

@Composable
private fun CourseInfoBadge(
    imageVector: ImageVector,
    infoText: String,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = infoText,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/*
    Some helper function to give the information on the card
 */
private fun CourseSummary.teacherText(): String = teachers.joinToString(separator = "、")

private fun CourseSummary.informationText(): String = listOf(
    classNo,
    "${credit.toInt()} 學分",
    teacherText(),
).joinToString(separator = " · ")

private fun CourseSummary.departmentText(): String = "$collegeName / $departmentName"

private fun CourseSummary.enrollmentText(): String = if (waitCnt > 0) {
    "$admitCnt / $limitCnt · 候補 $waitCnt"
} else {
    "$admitCnt / $limitCnt"
}

private fun CourseSummary.passwordText(): String = passwordCard.description

private fun CourseSummary.scheduleText(): String = classTimes.joinToString(separator = "、")

@Composable
@Preview
private fun PreviewCourseCard(
    courseSummary: CourseSummary = CourseSummary(
        serialNo = "36019",
        classNo = "ENA103-*",
        title = "專題討論（III）",
        credit = 0.0,
        passwordCard = PasswordCardType.NONE,
        teachers = listOf("鄭明敏", "林居慶", "林進榮", "林伯勳"),
        classTimes = listOf("5-A", "5-B", "5-C"),
        limitCnt = 0,
        admitCnt = 0,
        waitCnt = 0,
        collegeName = "工學院",
        departmentName = "環境工程研究所碩士班",
        courseType = CourseType.ELECTIVE,
        detailUrl = "https://cis.ncu.edu.tw/Course/main/support/courseDetail.html?crs=36019",
    ),
) {
    MaterialTheme {
        CourseCard(
            modifier = Modifier.width(360.dp),
            courseSummary = courseSummary,
            isSelected = false,
        )
        CourseCard(
            modifier = Modifier.width(360.dp),
            courseSummary = courseSummary,
            isSelected = true,
        )
    }
}
