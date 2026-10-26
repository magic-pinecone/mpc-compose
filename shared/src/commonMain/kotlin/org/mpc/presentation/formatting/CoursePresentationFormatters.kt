package org.mpc.presentation.formatting

import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType

internal fun Double.creditLabel(): String = toString().removeSuffix(".0")

/** 必修 or 選修; null when the catalog does not say, so callers omit it rather than show 未知. */
internal val CourseType.displayLabel: String?
    get() = takeIf { it != CourseType.UNKNOWN }?.description

internal fun CourseSummary.scheduleDescription(): String {
    val scheduleSlots = classTimes
        .filter { time -> time.day != CourseDay.UNKNOWN }
        .distinct()
        .sortedWith(compareBy<CourseTime> { time -> time.day.order }.thenBy { time -> time.period.order })
    if (scheduleSlots.isEmpty()) return "未提供上課時間"

    return scheduleSlots.joinToString("、") { time ->
        "${time.day.code}-${time.period.description}"
    }
}
