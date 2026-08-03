package org.mpc.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.mpc.data.local.database.CourseCatalogEntity
import org.mpc.data.local.database.CourseEntity
import org.mpc.data.local.database.CourseTeacherEntity
import org.mpc.data.local.database.CourseTimeEntity

@Dao
interface CourseCatalogDao {
    @Query("SELECT * FROM course_catalog WHERE semester = :semester")
    fun observeCatalogMetadata(semester: String): Flow<CourseCatalogEntity?>

    @Upsert
    suspend fun upsertCatalog(entity: CourseCatalogEntity)

    @Query("SELECT * FROM course_catalog WHERE semester = :semester")
    suspend fun findCatalog(semester: String): CourseCatalogEntity?

    @Query("DELETE FROM course_catalog WHERE semester = :semester")
    suspend fun deleteCatalog(semester: String)

    @Query("SELECT * FROM course WHERE semester = :semester ORDER BY serialNo")
    suspend fun findCourses(semester: String): List<CourseEntity>

    @Query("SELECT * FROM course_time WHERE semester = :semester")
    suspend fun findCourseTimes(semester: String): List<CourseTimeEntity>

    @Query("SELECT * FROM course_teacher WHERE semester = :semester")
    suspend fun findCourseTeachers(semester: String): List<CourseTeacherEntity>
}
