package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.mpc.data.dataSource.CourseDataSource
import org.mpc.data.mapper.dto.toDomain
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseResult
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.repository.CourseRepository

// TODO: Cache the result JSON fetch from remote
@ContributesBinding(AppScope::class)
@Inject
internal class DefaultCourseRepository(
    private val courseDataSource: CourseDataSource,
) : CourseRepository {
    override suspend fun fetchAllCourses(semester: String): CourseResult = courseDataSource.getAllCourses(semester).toDomain()

    override suspend fun fetchCourses(
        semester: String,
        query: String,
    ): CourseResult {
        val result = courseDataSource.getAllCourses(semester).toDomain()
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return result
        }

        val filteredCourse =
            result.courses.filter { course ->
                course.title.contains(
                    other = normalizedQuery,
                    ignoreCase = true,
                )
            }
        return result.copy(courses = filteredCourse)
    }

    override suspend fun fetchCoursesBySerialNo(
        semester: String,
        serialNos: List<CourseSerialNo>,
    ): CourseResult {
        val result = courseDataSource.getAllCourses(semester).toDomain()

        val serialNoSet = serialNos.toSet()

        val filteredCourse =
            result.courses.filter { course ->
                course.serialNo in serialNoSet
            }

        return result.copy(courses = filteredCourse)
    }

    override suspend fun fetchCourseDetail(
        semester: String,
        serialNo: String,
    ): CourseDetail = courseDataSource.getCourseDetails(semester, serialNo).toDomain()
}
