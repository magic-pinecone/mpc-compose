package org.mpc.domain.search

import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CourseQuery
import org.mpc.domain.model.CourseSummary

private const val FULL_WIDTH_OFFSET = 0xFEE0

private enum class MatchTier(
    val score: Int,
    val matches: (course: NormalizedCourse, token: String) -> Boolean,
) {
    SERIAL_NUMBER(60, { course, token -> course.serialNo == token }),
    TITLE_EQUALS(50, { course, token -> course.title == token }),
    TITLE_STARTS_WITH(40, { course, token -> course.title.startsWith(token) }),
    TITLE_CONTAINS(30, { course, token -> course.title.contains(token) }),
    TEACHER_CONTAINS(20, { course, token -> course.teachers.any { it.contains(token) } }),
    DEPARTMENT_CONTAINS(10, { course, token -> course.department.any { it.contains(token) } }),
}

private class NormalizedCourse(course: CourseSummary) {
    val serialNo = normalizeForSearch(course.serialNo.value)
    val title = normalizeForSearch(course.title)
    val teachers = course.teachers.map(::normalizeForSearch)
    val department = listOf(course.departmentName, course.collegeName).map(::normalizeForSearch)
}

fun normalizeForSearch(text: String): String = buildString {
    for (char in text) {
        append(
            when (char) {
                '　' -> ' '
                '臺' -> '台'
                in '！'..'～' -> (char.code - FULL_WIDTH_OFFSET).toChar().lowercaseChar()
                else -> char.lowercaseChar()
            },
        )
    }
}.trim().replace(Regex("\\s+"), " ")

fun searchCourses(
    courses: List<CourseSummary>,
    query: CourseQuery,
): List<CourseSummary> {
    val candidates = courses.filter { it.satisfiesFilters(query) }
    val tokens = normalizeForSearch(query.keyword).split(' ').filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return candidates

    return candidates
        .mapNotNull { course ->
            val normalized = NormalizedCourse(course)
            val scores = tokens.map { token -> scoreToken(normalized, token) ?: return@mapNotNull null }
            course to scores.sum()
        }
        .sortedByDescending { (_, score) -> score }
        .map { (course, _) -> course }
}

private fun scoreToken(
    course: NormalizedCourse,
    token: String,
): Int? = MatchTier.entries.firstOrNull { it.matches(course, token) }?.score

private fun CourseSummary.satisfiesFilters(query: CourseQuery): Boolean = matchesName(query.teacher, teachers) &&
    matchesName(query.department, listOf(departmentName, collegeName)) &&
    (query.courseType == null || courseType == query.courseType) &&
    meetsAt(query.days, query.periods)

private fun matchesName(
    wanted: String?,
    names: List<String>,
): Boolean {
    val normalizedWanted = wanted?.let(::normalizeForSearch).orEmpty()
    return normalizedWanted.isEmpty() || names.any { normalizeForSearch(it).contains(normalizedWanted) }
}

private fun CourseSummary.meetsAt(
    days: Set<CourseDay>,
    periods: Set<CoursePeriod>,
): Boolean = (days.isEmpty() && periods.isEmpty()) ||
    classTimes.any { (days.isEmpty() || it.day in days) && (periods.isEmpty() || it.period in periods) }
