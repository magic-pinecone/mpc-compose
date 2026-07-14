package org.mpc.domain.repository

import org.mpc.domain.model.entity.CourseDetail
import org.mpc.domain.model.entity.CourseResult

interface CourseRepository {
    suspend fun fetchAllCourses(semester: String): CourseResult

    suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetail
}