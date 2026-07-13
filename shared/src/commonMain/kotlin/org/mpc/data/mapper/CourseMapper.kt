package org.mpc.data.mapper

import org.mpc.data.dto.CourseDetailDto
import org.mpc.data.dto.CourseDto
import org.mpc.data.dto.CourseResultDto
import org.mpc.data.dto.CourseTypeDto
import org.mpc.data.dto.DistributionConditionDto
import org.mpc.data.dto.PasswordCardTypeDto
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseItem
import org.mpc.domain.model.CourseResult
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.DistributionCondition
import org.mpc.domain.model.PasswordCardType

internal fun CourseResultDto.toDomain(): CourseResult = CourseResult(
    lastUpdated = lastUpdated,
    academicYear = academicYear,
    semester = semester,
    courses = courses.map { it.toDomain() }
)

internal fun CourseDto.toDomain(): CourseItem = CourseItem(
    serialNo = serialNo,
    classNo = classNo,
    title = title,
    credit = credit,
    passwordCard = passwordCard.toDomain(),
    teachers = teachers,
    classTimes = classTimes,
    limitCnt = limitCnt,
    adminCnt = adminCnt,
    waitCnt = waitCnt,
    collegeName = collegeName,
    departmentName = departmentName,
    courseType = courseType.toDomain()
)

internal fun PasswordCardTypeDto.toDomain(): PasswordCardType =
    when(this) {
        PasswordCardTypeDto.REQUIRED -> PasswordCardType.REQUIRED
        PasswordCardTypeDto.OPTIONAL -> PasswordCardType.OPTIONAL
        PasswordCardTypeDto.UNKNOWN -> PasswordCardType.UNKNOWN
    }

internal fun CourseTypeDto.toDomain(): CourseType =
    when(this) {
        CourseTypeDto.REQUIRED -> CourseType.REQUIRED
        CourseTypeDto.OPTIONAL -> CourseType.OPTIONAL
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