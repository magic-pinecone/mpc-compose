package org.mpc.domain.repository

import org.mpc.data.dto.CourseDetailDto
import org.mpc.data.dto.CourseResultDto

interface CourseRepository {
    suspend fun fetchAllCourses(semester: String): CourseResultDto
    suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetailDto
}