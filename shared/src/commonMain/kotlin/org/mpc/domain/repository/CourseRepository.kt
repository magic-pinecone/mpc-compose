package org.mpc.domain.repository

import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseResult

interface CourseRepository {
    suspend fun fetchAllCourses(semester: String): CourseResult
    suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetail
}