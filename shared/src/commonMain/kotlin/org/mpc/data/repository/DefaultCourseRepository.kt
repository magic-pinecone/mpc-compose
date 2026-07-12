package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.mpc.data.dataSource.CourseDataSource
import org.mpc.data.dto.CourseDetailDto
import org.mpc.data.dto.CourseResultDto
import org.mpc.domain.repository.CourseRepository

@ContributesBinding(AppScope::class)
@Inject
internal class DefaultCourseRepository(
    private val courseDataSource: CourseDataSource,
): CourseRepository {
    override suspend fun fetchAllCourses(semester: String): CourseResultDto {
        return courseDataSource.getAllCourses(semester)
    }

    override suspend fun fetchCourseDetail(semester: String, serialNo: String): CourseDetailDto {
        return courseDataSource.getCourseDetails(semester, serialNo)
    }
}