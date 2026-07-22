package org.mpc.presentation.state

import org.mpc.domain.model.CourseResult

data class CourseSearchUiState(
    val semester: String,
    val query: String,
    val result: CourseSearchResultUiState = CourseSearchResultUiState.Loading,
)

sealed interface CourseSearchResultUiState {
    data object Loading : CourseSearchResultUiState

    data class Success(
        val result: CourseResult,
    ) : CourseSearchResultUiState

    data class Failure(
        val error: CourseSearchError,
    ) : CourseSearchResultUiState
}

enum class CourseSearchError {
    NETWORK,
    INVALID_SEMESTER,
    UNKNOWN,
}
