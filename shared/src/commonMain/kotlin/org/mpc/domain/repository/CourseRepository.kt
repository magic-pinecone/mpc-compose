package org.mpc.domain.repository

import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseResult
import org.mpc.domain.model.CourseSerialNo

interface CourseRepository {
    suspend fun fetchAllCourses(semester: String): CourseResult

    suspend fun fetchCourses(
        semester: String,
        query: String,
    ): CourseResult

    suspend fun fetchCoursesBySerialNo(
        semester: String,
        serialNos: List<CourseSerialNo>,
    ): CourseResult

    suspend fun fetchCourseDetail(
        semester: String,
        serialNo: String,
    ): CourseDetail
}
