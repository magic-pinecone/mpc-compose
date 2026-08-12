package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.repository.CoursePlanRepository
import org.mpc.presentation.state.CoursePlanDraftStore
import org.mpc.presentation.state.CoursePlanUiState

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>(),
)
class CourseSelectionViewModel(
    private val coursePlanRepository: CoursePlanRepository,
    private val draftStore: CoursePlanDraftStore,
) : ViewModel() {
    // TODO: stop hard code semester and load plan from app storage

    private val semester = "115-1"

    val uiState = draftStore.uiState

    private var saveJob: Job? = null

    init {
        loadPlan()
    }

    private fun loadPlan() {
        if (!draftStore.shouldLoad(semester)) {
            return
        }

        viewModelScope.launch {
            runCatching {
                coursePlanRepository.loadPlan(semester)
            }.onSuccess(draftStore::acceptLoadedPlan)
                .onFailure { cause ->
                    cause.rethrowIfCancellationOrFatal()
                    draftStore.acceptLoadFailure(cause)
                }
        }
    }

    fun toggleCourse(course: CourseSummary) {
        draftStore.toggleCourse(course)
    }

    fun savePlan() {
        val plan = (uiState.value as? CoursePlanUiState.Success)?.plan
            ?: return

        if (saveJob?.isActive == true) {
            return
        }

        saveJob =
            viewModelScope.launch {
                runCatching {
                    coursePlanRepository.savePlan(plan)
                }.onFailure { cause ->
                    cause.rethrowIfCancellationOrFatal()
                    Logger.e(cause.toString())
                }
            }
    }
}
