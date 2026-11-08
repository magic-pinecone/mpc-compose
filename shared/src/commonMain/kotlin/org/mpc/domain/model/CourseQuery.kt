package org.mpc.domain.model

data class CourseQuery(
    val semester: String,
    val keyword: String = "",
    val teacher: String? = null,
    val department: String? = null,
    val days: Set<CourseDay> = emptySet(),
    val periods: Set<CoursePeriod> = emptySet(),
    val courseType: CourseType? = null,
)
