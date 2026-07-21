package org.mpc.presentation.state

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import org.mpc.domain.model.snapshot.addCourse
import org.mpc.domain.model.snapshot.removeCourse
import org.mpc.domain.model.state.CoursePlanState

@Inject
@SingleIn(AppScope::class)
class CoursePlanDraftStore {
    private val _state = MutableStateFlow<CoursePlanState>(CoursePlanState.Loading)

    val state = _state.asStateFlow()

    fun shouldLoad(semester: String): Boolean {
        val currentState = state.value
        return currentState !is CoursePlanState.Success ||
            currentState.snapshot.semester != semester
    }

    fun acceptLoadedSnapshot(snapshot: CoursePlanSnapshot) {
        _state.update { currentState ->
            if (
                currentState is CoursePlanState.Success &&
                currentState.snapshot.semester == snapshot.semester
            ) {
                currentState
            } else {
                CoursePlanState.Success(snapshot)
            }
        }
    }

    fun acceptLoadFailure(cause: Throwable) {
        _state.update { currentState ->
            if (currentState is CoursePlanState.Loading) {
                CoursePlanState.Failure(cause)
            } else {
                currentState
            }
        }
    }

    fun toggleCourse(course: CourseSummary) {
        _state.update { currentState ->
            if (currentState !is CoursePlanState.Success) {
                return@update currentState
            }

            val updatedSnapshot = if (
                currentState.snapshot.selected.containsKey(course.serialNo)
            ) {
                currentState.snapshot.removeCourse(course)
            } else {
                currentState.snapshot.addCourse(course)
            }

            CoursePlanState.Success(updatedSnapshot)
        }
    }
}
