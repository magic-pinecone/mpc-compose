package org.mpc.data.mapper

import co.touchlab.kermit.Logger
import org.mpc.data.dto.CourseDetailDto
import org.mpc.data.dto.CourseDto
import org.mpc.data.dto.CourseResultDto
import org.mpc.data.dto.CourseTypeDto
import org.mpc.data.dto.DistributionConditionDto
import org.mpc.data.dto.PasswordCardTypeDto
import org.mpc.domain.model.entity.CourseDay
import org.mpc.domain.model.entity.CourseDetail
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.model.entity.CoursePeriod
import org.mpc.domain.model.entity.CourseSerialNo
import org.mpc.domain.model.entity.CourseTime
import org.mpc.domain.model.entity.CourseType
import org.mpc.domain.model.entity.DistributionCondition
import org.mpc.domain.model.entity.PasswordCardType

internal fun CourseResultDto.toDomain(): CourseResult = CourseResult(
    lastUpdated = lastUpdated,
    semester = "$academicYear-$semester",
    courses = courses.map { it.toDomain() }
)

internal fun CourseDto.toDomain(): CourseSummary = CourseSummary(
    serialNo = CourseSerialNo(serialNo),
    classNo = classNo,
    title = title,
    credit = credit,
    passwordCard = passwordCard.toDomain(),
    teachers = teachers,
    classTimes = classTimes.mapNotNull { rawTime ->
        rawTime.toCourseTimeOrNull().also { parsed ->
            if (parsed == null) {
                Logger.w { "Ignoring invalid course time $parsed" }
            }
        }
    },
    limitCnt = limitCnt,
    admitCnt = admitCnt,
    waitCnt = waitCnt,
    collegeName = collegeName,
    departmentName = departmentName,
    courseType = courseType.toDomain(),
    detailUrl = detailUrl
)

private fun String.toCourseTimeOrNull(): CourseTime? {
    val parts = split("-", limit = 2)
    if (parts.size != 2) {
        return null
    }

    val day = CourseDay.entries
        .firstOrNull { it.code == parts[0] }
        ?: return null

    val period = CoursePeriod.entries
        .firstOrNull { it.description == parts[1] }
        ?: return null


    return CourseTime(
        day = day,
        period = period
    )
}

internal fun PasswordCardTypeDto.toDomain(): PasswordCardType =
    when(this) {
        PasswordCardTypeDto.ALL -> PasswordCardType.ALL
        PasswordCardTypeDto.OPTIONAL -> PasswordCardType.OPTIONAL
        PasswordCardTypeDto.NONE -> PasswordCardType.NONE
    }

internal fun CourseTypeDto.toDomain(): CourseType =
    when(this) {
        CourseTypeDto.REQUIRED -> CourseType.REQUIRED
        CourseTypeDto.ELECTIVE -> CourseType.ELECTIVE
        CourseTypeDto.UNKNOWN -> CourseType.UNKNOWN
    }

internal fun CourseDetailDto.toDomain(): CourseDetail = CourseDetail(
    serialNo = serialNo,
    objectives = objectives,
    content = content,
    books = books,
    teachingMethod = teachingMethod,
    gradingPolicy = gradingPolicy,
    distributionConditions = distributionConditions.map { it.toDomain() }
)

internal fun DistributionConditionDto.toDomain(): DistributionCondition = DistributionCondition(
    priority = priority,
    rule = rule
)
