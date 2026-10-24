package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import org.mpc.domain.portal.EECLASS_TIMETABLE_PATH
import org.mpc.domain.portal.EeclassSessionManager

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>(),
)
class EeclassSessionViewModel(
    private val sessionManager: EeclassSessionManager,
) : ViewModel() {
    val state = sessionManager.state
    val pendingTaskPath = sessionManager.pendingTaskPath

    fun restore() {
        viewModelScope.launch { sessionManager.restore() }
    }

    suspend fun browserNavigationStarted() {
        sessionManager.browserNavigationStarted()
    }

    suspend fun browserNavigationFinished(url: String) {
        sessionManager.browserNavigationFinished(url)
    }

    fun requestTask(path: String = EECLASS_TIMETABLE_PATH) {
        sessionManager.requestTask(path)
    }

    fun pendingTaskUrlIfAuthenticated(): String? = sessionManager.pendingTaskUrlIfAuthenticated()
}
