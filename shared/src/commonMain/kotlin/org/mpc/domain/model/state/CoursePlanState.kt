package org.mpc.domain.model.state

import org.mpc.domain.model.snapshot.CoursePlanSnapshot

sealed interface CoursePlanState {
    data object Loading: CoursePlanState
    data class Success(val snapshot: CoursePlanSnapshot): CoursePlanState
    data class Failure(val cause: Throwable): CoursePlanState
}