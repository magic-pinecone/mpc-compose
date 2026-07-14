package org.mpc.domain.model.state

import org.mpc.domain.model.entity.CourseResult
import org.mpc.domain.model.entity.CourseSummary

data class CourseSearchState(
    val semester: String,
    val query: String,
    val result: CourseLoadState = CourseLoadState.Idle
)

sealed interface CourseLoadState {
    data object Idle: CourseLoadState
    data object Loading: CourseLoadState
    data class Success(
        val courses: CourseResult
    ): CourseLoadState
    data class Failure(
        val error: CourseSearchError
    ): CourseLoadState
}



enum class CourseSearchError {
    NETWORK,
    INVALID_SEMESTER,
    UNKNOWN
}