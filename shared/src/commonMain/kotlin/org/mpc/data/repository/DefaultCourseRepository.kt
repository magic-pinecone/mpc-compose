package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.mpc.data.dataSource.CourseDataSource
import org.mpc.data.mapper.toDomain
import org.mpc.domain.model.entity.CourseDetail
import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.repository.CourseRepository

// TODO: Cache the result json fetch from remote
@ContributesBinding(AppScope::class)
@Inject
internal class DefaultCourseRepository(
    private val courseDataSource: CourseDataSource,
): CourseRepository {
    override suspend fun fetchAllCourses(semester: String): CourseResult {
        return courseDataSource.getAllCourses(semester).toDomain()
    }

    override suspend fun fetchCourses(semester: String, query: String): CourseResult {
        val result = courseDataSource.getAllCourses(semester).toDomain()
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return result
        }

        val filteredCourse = result.courses.filter {
            course -> course.title.contains(
                other = normalizedQuery,
                ignoreCase = true
            )
        }
        return result.copy(courses = filteredCourse)
    }

    override suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetail {
        return courseDataSource.getCourseDetails(semester, serialNo).toDomain()
    }
}