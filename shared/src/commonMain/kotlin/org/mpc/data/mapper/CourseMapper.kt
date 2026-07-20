package org.mpc.data.mapper

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
    classTimes = classTimes.map { it.toCourseTime() },
    limitCnt = limitCnt,
    admitCnt = admitCnt,
    waitCnt = waitCnt,
    collegeName = collegeName,
    departmentName = departmentName,
    courseType = courseType.toDomain(),
    detailUrl = detailUrl
)

private fun String.toCourseTime(): CourseTime {
    val parts = split("-", limit = 2)
    require(parts.size == 2) {
        "Invalid course time '$this': expected <day>-<period>"
    }

    return CourseTime(
        day = when (parts[0]) {
            "0" -> CourseDay.UNKNOWN
            "1" -> CourseDay.MONDAY
            "2" -> CourseDay.TUESDAY
            "3" -> CourseDay.WEDNESDAY
            "4" -> CourseDay.THURSDAY
            "5" -> CourseDay.FRIDAY
            "6" -> CourseDay.SATURDAY
            "7" -> CourseDay.SUNDAY
            else -> throw IllegalArgumentException(
                "Invalid course day '${parts[0]}' in '$this'"
            )
        },
        period = when (parts[1]) {
            "1" -> CoursePeriod.ONE
            "2" -> CoursePeriod.TWO
            "3" -> CoursePeriod.THREE
            "4" -> CoursePeriod.FOUR
            "Z" -> CoursePeriod.NOON
            "5" -> CoursePeriod.FIVE
            "6" -> CoursePeriod.SIX
            "7" -> CoursePeriod.SEVEN
            "8" -> CoursePeriod.EIGHT
            "9" -> CoursePeriod.NINE
            "A" -> CoursePeriod.A
            "B" -> CoursePeriod.B
            "C" -> CoursePeriod.C
            "D" -> CoursePeriod.D
            else -> throw IllegalArgumentException(
                "Invalid course period '${parts[1]}' in '$this'"
            )
        },
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
