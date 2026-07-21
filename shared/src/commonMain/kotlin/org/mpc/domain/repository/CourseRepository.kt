package org.mpc.domain.repository

import org.mpc.domain.model.entity.CourseDetail
import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.model.entity.CourseSerialNo

interface CourseRepository {
    suspend fun fetchAllCourses(semester: String): CourseResult

    suspend fun fetchCourses(semester: String, query: String): CourseResult

    suspend fun fetchCoursesBySerialNo(semester: String, serialNos: List<CourseSerialNo>): CourseResult

    suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetail
}