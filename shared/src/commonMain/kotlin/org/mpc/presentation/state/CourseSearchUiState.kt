package org.mpc.presentation.state

import org.mpc.domain.model.CourseFilter
import org.mpc.domain.model.CourseResult

data class CourseSearchUiState(
    val semester: String,
    val query: String,
    val filters: List<CourseFilter> = emptyList(),
    val suggestions: List<CourseFilter> = emptyList(),
    val filterOptions: CourseFilterOptions = CourseFilterOptions(),
    val result: CourseSearchResultUiState = CourseSearchResultUiState.Loading,
)

data class CourseFilterOptions(
    val teachers: List<String> = emptyList(),
    val departments: List<String> = emptyList(),
    val colleges: List<String> = emptyList(),
    val credits: List<Double> = emptyList(),
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
