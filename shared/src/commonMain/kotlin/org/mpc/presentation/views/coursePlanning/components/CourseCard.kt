package org.mpc.presentation.views.coursePlanning.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable
import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.PasswordCardType
import org.mpc.presentation.icon.apartment
import org.mpc.presentation.icon.groups
import org.mpc.presentation.icon.key
import org.mpc.presentation.icon.schedule
import org.mpc.presentation.theme.MpcTheme

@Composable
fun CourseCard(
    modifier: Modifier = Modifier,
    courseSummary: CourseSummary,
    isSelected: Boolean,
    onCardClick: () -> Unit = {},
    onButtonClick: () -> Unit = {},
    isCompact: Boolean = false,
    canEditPlan: Boolean = true,
) {
    val cardShape = MaterialTheme.shapes.large

    Surface(
        modifier =
        modifier.semantics {
            stateDescription = if (isSelected) "已加入課表" else "尚未加入課表"
        }.adaptiveClickable(
            shape = cardShape,
            role = Role.Button,
            onClick = onCardClick,
        ),
        shape = cardShape,
        color =
        if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border =
        BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        if (isCompact) {
            CompactCourseCardContent(
                courseSummary = courseSummary,
                isSelected = isSelected,
                canEditPlan = canEditPlan,
                onToggleSelection = onButtonClick,
            )
        } else {
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
                    CourseTypeBadge(courseSummary.courseType, isCompact = false)
                }
                Text(
                    text = courseSummary.primaryMetadataText(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    AdaptiveButton(
                        onClick = onButtonClick,
                        enabled = canEditPlan,
                        modifier = Modifier.heightIn(min = 48.dp).semantics {
                            contentDescription = courseSummary.selectionActionDescription(isSelected)
                        },
                        colors =
                        if (isSelected) {
                            ButtonDefaults.filledTonalButtonColors()
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        liquidGlassColors = if (isSelected) {
                            LiquidGlassButtonColors(
                                tintColor = MaterialTheme.colorScheme.secondaryContainer,
                                surfaceColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                            )
                        } else {
                            null
                        },
                        contentPadding = ButtonDefaults.ContentPadding,
                    ) {
                        Text(if (isSelected) "移除" else "加入")
                    }
                }
            }
        }
    }
}

@Composable
internal fun CourseTypeBadge(
    courseType: CourseType,
    isCompact: Boolean,
) {
    Box(
        modifier =
        Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
            ).padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = if (isCompact) 3.dp else 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = courseType.description,
            maxLines = if (isCompact) 1 else 2,
            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun CompactCourseSelectionControl(
    isSelected: Boolean,
    canEditPlan: Boolean,
    courseSummary: CourseSummary,
    onClick: () -> Unit,
) {
    AdaptiveIconButton(
        onClick = onClick,
        enabled = canEditPlan,
        modifier = Modifier.size(48.dp).semantics {
            contentDescription = courseSummary.selectionActionDescription(isSelected)
        },
        colors = if (isSelected) {
            IconButtonDefaults.filledTonalIconButtonColors()
        } else {
            IconButtonDefaults.iconButtonColors()
        },
        liquidGlassColors = if (isSelected) {
            LiquidGlassButtonColors(
                tintColor = MaterialTheme.colorScheme.secondaryContainer,
                surfaceColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
            )
        } else {
            null
        },
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.Remove else Icons.Default.Add,
            contentDescription = null,
        )
    }
}

@Composable
private fun CourseInfoRail(courseSummary: CourseSummary) {
    Row(
        modifier =
        Modifier
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
        modifier =
        Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.extraSmall,
            ).padding(horizontal = 8.dp, vertical = 5.dp),
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
private fun CourseSummary.selectionActionDescription(isSelected: Boolean): String = if (isSelected) {
    "從課表移除 $title"
} else {
    "加入課表 $title"
}

private fun CourseSummary.primaryMetadataText(): String {
    val instructorText = teachers.filter { it.isNotBlank() }.joinToString("、").ifBlank { "未提供授課教師" }
    return listOf(classNo, "${credit.toString().removeSuffix(".0")} 學分", instructorText)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
}

private fun CourseSummary.departmentText(): String = "$collegeName / $departmentName"

private fun CourseSummary.enrollmentText(): String = if (waitCnt > 0) {
    "$admitCnt / $limitCnt · 候補 $waitCnt"
} else {
    "$admitCnt / $limitCnt"
}

private fun CourseSummary.passwordText(): String = passwordCard.description

internal fun CourseSummary.scheduleText(): String {
    val scheduleSlots = classTimes
        .filter { time -> time.day != CourseDay.UNKNOWN }
        .distinct()
        .sortedWith(compareBy<CourseTime> { time -> time.day.order }.thenBy { time -> time.period.order })
    if (scheduleSlots.isEmpty()) return "未提供上課時間"

    return scheduleSlots.joinToString("、") { time ->
        "${time.day.code}-${time.period.description}"
    }
}

@Composable
@Preview
internal fun PreviewCourseCard(
    courseSummary: CourseSummary =
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
) {
    MpcTheme {
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
