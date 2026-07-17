package org.mpc.domain.repository

import org.mpc.domain.model.entity.CourseResult

data class CourseSearchState(
    val semester: String,
    val query: String,
    val result: CourseLoadState = CourseLoadState.Loading
)

sealed interface CourseLoadState {
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