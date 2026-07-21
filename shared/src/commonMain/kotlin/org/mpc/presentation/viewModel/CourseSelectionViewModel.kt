package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.repository.CoursePlanRepository
import org.mpc.presentation.state.CoursePlanDraftStore

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>()
)
class CourseSelectionViewModel(
    private val coursePlanRepository: CoursePlanRepository,
    private val draftStore: CoursePlanDraftStore,
) : ViewModel() {
    // TODO: stop hard code semester and load plan from app storage

    private val semester = "115-1"

    val state = draftStore.state

    init {
        loadPlan()
    }

    private fun loadPlan() {
        if (!draftStore.shouldLoad(semester)) {
            return
        }

        viewModelScope.launch {
            try {
                draftStore.acceptLoadedSnapshot(
                    coursePlanRepository.loadPlan(semester)
                )
            } catch (exception: Exception) {
                draftStore.acceptLoadFailure(exception)
            }
        }
    }

    fun toggleCourse(course: CourseSummary) {
        draftStore.toggleCourse(course)
    }
}
