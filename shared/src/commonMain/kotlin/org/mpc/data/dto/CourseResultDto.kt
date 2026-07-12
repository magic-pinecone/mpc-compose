package org.mpc.data.dto

import kotlin.time.Instant

/*
{
  "last_updated": "2026-07-12T09:06:54.266949+00:00",
  "academic_year": "115",
  "semester": "1",
  "courses": []
}
 */

data class CourseResultDto(
    val lastUpdated: Instant,
    val academicYear: String,
    val semester: String,
    val courses: Array<CourseDto>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CourseResultDto

        if (lastUpdated != other.lastUpdated) return false
        if (academicYear != other.academicYear) return false
        if (semester != other.semester) return false
        if (!courses.contentEquals(other.courses)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastUpdated.hashCode()
        result = 31 * result + academicYear.hashCode()
        result = 31 * result + semester.hashCode()
        result = 31 * result + courses.contentHashCode()
        return result
    }
}
