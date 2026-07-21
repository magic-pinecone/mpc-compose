package org.mpc.domain.repository

import org.mpc.domain.model.snapshot.CoursePlanSnapshot

interface CoursePlanRepository {
    suspend fun loadPlan(semester: String): CoursePlanSnapshot
    suspend fun savePlan(snapshot: CoursePlanSnapshot)
    suspend fun deletePlan(semester: String)
}