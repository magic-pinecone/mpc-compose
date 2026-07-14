package org.mpc.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mpc.domain.model.state.CourseLoadState
import org.mpc.domain.model.state.CourseSearchError
import org.mpc.domain.model.state.CourseSearchState
import org.mpc.domain.repository.CourseRepository

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>()
)
class CourseSearchViewModel(
    private val courseRepository: CourseRepository,
):
    ViewModel() {
    // TODO: update semester so it's not hard coded
    private val _state: MutableStateFlow<CourseSearchState> = MutableStateFlow(
        CourseSearchState(semester = "115-1", query = "")
    )

    val state = _state.asStateFlow()

    fun updateQuery(semester: String, query: String) {
        _state.update {
            it.copy(semester=semester, query=query)
        }
    }

    fun onSearch() {
        val semester = state.value.semester
        val query = state.value.query
        // TODO: do proper validation
        if (semester != "115-1") {
            _state.update {
                it.copy(result = CourseLoadState.Failure(CourseSearchError.INVALID_SEMESTER))
            }
            return
        }

        _state.update {
            it.copy(result = CourseLoadState.Loading)
        }

        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _state.update {
                        it.copy(
                            result = CourseLoadState.Success(
                                courses = courseRepository.fetchAllCourses(semester)
                            )
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            result = CourseLoadState.Success(
                                courses = courseRepository.fetchCourses(semester, query)
                            )
                        )
                    }
                }
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        result = CourseLoadState.Failure(
                            error = CourseSearchError.UNKNOWN
                        )
                    )
                }
            }
        }
    }
}