package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.mpc.data.local.dao.CoursePlanDao
import org.mpc.data.mapper.entities.toCoursePlanEntity
import org.mpc.data.mapper.entities.toCoursePlanItemsEntity
import org.mpc.domain.model.entity.CourseSerialNo
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import org.mpc.domain.repository.CoursePlanRepository
import org.mpc.domain.repository.CourseRepository

@ContributesBinding(AppScope::class)
@Inject
class LocalCoursePlanRepository(
    private val courseRepository: CourseRepository,
    private val coursePlanDao: CoursePlanDao
): CoursePlanRepository {
    override suspend fun loadPlan(semester: String): CoursePlanSnapshot {
        val selectedCourses = coursePlanDao.findSelectedSerialNumbers(semester).map { CourseSerialNo(it) }

        if (selectedCourses.isEmpty()) {
            return CoursePlanSnapshot(
                semester = semester,
                selected = mapOf()
            )
        }

        val courseResult = courseRepository.fetchCoursesBySerialNo(semester, selectedCourses)

        return CoursePlanSnapshot(
            semester = semester,
            selected = courseResult.courses.associateBy { it.serialNo }
        )
    }

    override suspend fun savePlan(snapshot: CoursePlanSnapshot) {
        coursePlanDao.replaceItems(snapshot.toCoursePlanEntity(), snapshot.toCoursePlanItemsEntity())
    }

    override suspend fun deletePlan(semester: String) {
        coursePlanDao.deletePlan(semester)
    }

}