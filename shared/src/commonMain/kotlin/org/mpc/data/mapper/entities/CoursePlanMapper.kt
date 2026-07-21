package org.mpc.data.mapper.entities

import org.mpc.data.local.database.CoursePlanEntity
import org.mpc.data.local.database.CoursePlanItemEntity
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import kotlin.time.Clock

internal fun CoursePlanSnapshot.toCoursePlanEntity(): CoursePlanEntity = CoursePlanEntity(
    semester = semester,
    updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds()
)

internal fun CoursePlanSnapshot.toCoursePlanItemsEntity(): List<CoursePlanItemEntity> =
    selected.values.map {
        CoursePlanItemEntity(
            semester = semester,
            serialNo = it.serialNo.value
        )
    }

