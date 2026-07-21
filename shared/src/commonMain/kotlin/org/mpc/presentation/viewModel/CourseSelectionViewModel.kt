package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.snapshot.addCourse
import org.mpc.domain.model.snapshot.removeCourse
import org.mpc.domain.model.state.CoursePlanState
import org.mpc.domain.repository.CoursePlanRepository

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>()
)
class CourseSelectionViewModel(
    private val coursePlanRepository: CoursePlanRepository
) : ViewModel() {
    // TODO: stop hard code semester and load plan from app storage

    private val semester = "115-1"
    private val _state = MutableStateFlow<CoursePlanState>(CoursePlanState.Loading)

    val state = _state.asStateFlow()

    init {
        loadPlan()
    }

    private fun loadPlan() {
        viewModelScope.launch {
            _state.value = try {
                CoursePlanState.Success(
                    coursePlanRepository.loadPlan(semester)
                )
            } catch (exception: Exception) {
                CoursePlanState.Failure(exception)
            }
        }
    }

    fun toggleCourse(course: CourseSummary) {
        val currentState = state.value
        if (currentState !is CoursePlanState.Success) {
            return
        }
        if (currentState.snapshot.selected.contains(course.serialNo)) {
            _state.value = CoursePlanState.Success(
                snapshot = currentState.snapshot.removeCourse(course)
            )
        } else {
            _state.value = CoursePlanState.Success(
                snapshot = currentState.snapshot.addCourse(course)
            )
        }
    }

}