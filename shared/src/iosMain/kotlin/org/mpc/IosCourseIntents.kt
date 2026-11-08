package org.mpc

import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CourseFilter
import org.mpc.domain.model.CourseQuery
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.schedule.ClassMeeting
import org.mpc.domain.schedule.UpcomingMeeting
import org.mpc.domain.schedule.meetingsOn
import org.mpc.domain.schedule.nextMeeting

private const val SERIAL_NUMBER_LENGTH = 5

/** Course Catalog lookup for App Intents; [limit] caps the ranked results. */
@Throws(Exception::class)
suspend fun IosSharedHost.findCourses(
    semester: String,
    keyword: String,
    filters: List<CourseFilter>,
    limit: Int,
): List<CourseSummary> = appGraph.courseRepository
    .searchCourses(CourseQuery(semester, keyword, filters.toSet()))
    .courses
    .take(limit.coerceAtLeast(0))

/** The course with this serial number, or null when it is malformed or not in the catalog. */
@Throws(Exception::class)
suspend fun IosSharedHost.course(
    semester: String,
    serialNumber: String,
): CourseSummary? {
    if (serialNumber.length != SERIAL_NUMBER_LENGTH || !serialNumber.all { it in '0'..'9' }) return null
    return appGraph.courseRepository
        .fetchCoursesBySerialNo(semester, listOf(CourseSerialNo(serialNumber)))
        .courses
        .firstOrNull()
}

/** Meetings of the Saved Course Plan on [day], in start order. */
@Throws(Exception::class)
suspend fun IosSharedHost.savedMeetingsOn(
    semester: String,
    day: CourseDay,
): List<ClassMeeting> = appGraph.coursePlanRepository.loadPlan(semester).meetingsOn(day)

@Throws(Exception::class)
suspend fun IosSharedHost.nextSavedMeeting(
    semester: String,
    day: CourseDay,
    minuteOfDay: Int,
): UpcomingMeeting? = appGraph.coursePlanRepository.loadPlan(semester).nextMeeting(day, minuteOfDay)
