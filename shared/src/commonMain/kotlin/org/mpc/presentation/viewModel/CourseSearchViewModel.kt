package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.state.CourseSearchError
import org.mpc.presentation.state.CourseSearchResultUiState
import org.mpc.presentation.state.CourseSearchUiState

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>(),
)
class CourseSearchViewModel(
    private val courseRepository: CourseRepository,
) : ViewModel() {
    // TODO: update semester so it's not hard coded
    private val _uiState: MutableStateFlow<CourseSearchUiState> =
        MutableStateFlow(
            CourseSearchUiState(semester = "115-1", query = ""),
        )

    val uiState = _uiState.asStateFlow()

    init {
        loadAllCourses()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadAllCourses() {
        _uiState.update {
            it.copy(result = CourseSearchResultUiState.Loading)
        }

        viewModelScope.launch {
            val semester = uiState.value.semester
            try {
                _uiState.update {
                    it.copy(
                        result =
                            CourseSearchResultUiState.Success(
                                result = courseRepository.fetchAllCourses(semester),
                            ),
                    )
                }
            } catch (exception: Exception) {
                Logger.i(exception.toString())
                _uiState.update {
                    it.copy(
                        result = CourseSearchResultUiState.Failure(error = CourseSearchError.UNKNOWN),
                    )
                }
            }
        }
    }

    fun updateQuery(
        semester: String,
        query: String,
    ) {
        _uiState.update {
            it.copy(semester = semester, query = query)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun onSearch() {
        val semester = uiState.value.semester
        val query = uiState.value.query
        // TODO: do proper validation
        if (semester != "115-1") {
            _uiState.update {
                it.copy(result = CourseSearchResultUiState.Failure(CourseSearchError.INVALID_SEMESTER))
            }
            return
        }

        _uiState.update {
            it.copy(result = CourseSearchResultUiState.Loading)
        }

        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _uiState.update {
                        it.copy(
                            result =
                                CourseSearchResultUiState.Success(
                                    result = courseRepository.fetchAllCourses(semester),
                                ),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            result =
                                CourseSearchResultUiState.Success(
                                    result = courseRepository.fetchCourses(semester, query),
                                ),
                        )
                    }
                }
            } catch (exception: Exception) {
                Logger.i(exception.toString())
                _uiState.update {
                    it.copy(
                        result =
                            CourseSearchResultUiState.Failure(
                                error = CourseSearchError.UNKNOWN,
                            ),
                    )
                }
            }
        }
    }
}
