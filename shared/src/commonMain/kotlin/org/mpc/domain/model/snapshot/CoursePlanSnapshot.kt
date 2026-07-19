package org.mpc.domain.model.snapshot

import org.mpc.domain.model.entity.CourseSummary


data class CoursePlanSnapshot(
    val semester: String,
    val selected: Map<String, CourseSummary>,
)

fun CoursePlanSnapshot.addCourse(course: CourseSummary): CoursePlanSnapshot = copy(
    selected = selected + (course.serialNo to course),
)

fun CoursePlanSnapshot.removeCourse(course: CourseSummary): CoursePlanSnapshot = copy(
    selected = selected - course.serialNo,
)
