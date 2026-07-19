package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import org.mpc.domain.model.snapshot.addCourse
import org.mpc.domain.model.snapshot.removeCourse

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>()
)
class CourseSelectionViewModel : ViewModel() {
    // TODO: stop hard code semester and load plan from app storage
    private val _state = MutableStateFlow(
        CoursePlanSnapshot(
            semester = "115-1",
            selected = mapOf(),
        )
    )

    val state = _state.asStateFlow()

    fun toggleCourse(course: CourseSummary) {
        _state.update {
            if (it.selected.containsKey(course.serialNo)) {
                it.removeCourse(course)
            } else {
                it.addCourse(course)
            }
        }
    }

}