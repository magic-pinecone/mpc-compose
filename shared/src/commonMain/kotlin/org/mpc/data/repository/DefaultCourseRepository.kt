package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.mpc.data.dataSource.CourseDataSource
import org.mpc.data.mapper.toDomain
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.CourseResult
import org.mpc.domain.repository.CourseRepository

@ContributesBinding(AppScope::class)
@Inject
internal class DefaultCourseRepository(
    private val courseDataSource: CourseDataSource,
): CourseRepository {
    override suspend fun fetchAllCourses(semester: String): CourseResult {
        return courseDataSource.getAllCourses(semester).toDomain()
    }

    override suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetail {
        return courseDataSource.getCourseDetails(semester, serialNo).toDomain()
    }
}