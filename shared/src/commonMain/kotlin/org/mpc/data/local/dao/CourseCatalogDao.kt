package org.mpc.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.mpc.data.local.database.CourseCatalogEntity

@Dao
interface CourseCatalogDao {
    @Upsert
    suspend fun upsertCatalog(entity: CourseCatalogEntity)

    @Query("SELECT * FROM course_catalog WHERE semester = :semester")
    suspend fun findCatalog(semester: String): CourseCatalogEntity?

    @Query("DELETE FROM course_catalog WHERE semester = :semester")
    suspend fun deleteCatalog(semester: String)
}
