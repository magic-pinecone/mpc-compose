package org.mpc

import androidx.lifecycle.ViewModel
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.mpc.di.AppGraph
import org.mpc.domain.model.AppSettings
import org.mpc.domain.model.AppThemeMode
import org.mpc.domain.model.PortalAuthenticationMode
import org.mpc.domain.model.TimetableColorMode
import org.mpc.domain.repository.AppSettingsRepository
import org.mpc.domain.repository.CourseRepository
import org.mpc.presentation.state.CoursePlanDraftStore
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosSharedHostTest {
    @Test
    fun themeWaitsForPreferencesAndUpdatesWithoutAComposeController() = runTest {
        val repository = HostSettingsRepository()
        val host = IosSharedHost(HostTestGraph(repository), settingsScope = backgroundScope)
        val received = mutableListOf<AppThemeMode>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            host.themeMode.collect { received.add(it) }
        }
        testScheduler.runCurrent()
        assertTrue(received.isEmpty())

        repository.settings.emit(AppSettings(themeMode = AppThemeMode.DARK))
        testScheduler.runCurrent()
        repository.settings.emit(AppSettings(themeMode = AppThemeMode.DARK, timetableColorMode = TimetableColorMode.SHUFFLED))
        testScheduler.runCurrent()
        repository.settings.emit(AppSettings(themeMode = AppThemeMode.LIGHT))
        testScheduler.runCurrent()

        assertEquals(listOf(AppThemeMode.DARK, AppThemeMode.LIGHT), received)
        collector.cancelAndJoin()
    }

    @Test
    fun themeReplaysLatestPreferencesAfterCollectionRestarts() = runTest {
        val repository = HostSettingsRepository()
        val host = IosSharedHost(HostTestGraph(repository), settingsScope = backgroundScope)
        val received = mutableListOf<AppThemeMode>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            host.themeMode.collect { received.add(it) }
        }
        repository.settings.emit(AppSettings(themeMode = AppThemeMode.DARK))
        testScheduler.runCurrent()
        collector.cancelAndJoin()
        repository.settings.emit(AppSettings(themeMode = AppThemeMode.LIGHT))
        testScheduler.runCurrent()
        assertEquals(listOf(AppThemeMode.DARK), received)

        val restartedCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            host.themeMode.collect { received.add(it) }
        }
        assertEquals(listOf(AppThemeMode.DARK, AppThemeMode.LIGHT), received)
        restartedCollector.cancelAndJoin()
    }
}

private class HostSettingsRepository : AppSettingsRepository {
    override val settings = MutableSharedFlow<AppSettings>(replay = 1)

    override suspend fun setThemeMode(themeMode: AppThemeMode): Unit = error("Settings writes are unused by the host")

    override suspend fun setPortalAuthenticationMode(authenticationMode: PortalAuthenticationMode): Unit = error("Settings writes are unused by the host")

    override suspend fun setTimetableColorMode(colorMode: TimetableColorMode): Unit = error("Settings writes are unused by the host")
}

private class HostTestGraph(
    override val appSettingsRepository: AppSettingsRepository,
) : AppGraph {
    override val courseRepository: CourseRepository get() = error("No course request expected")
    override val coursePlanDraftStore: CoursePlanDraftStore get() = error("No Compose controller expected")
    override val metroViewModelFactory: MetroViewModelFactory get() = error("No Compose controller expected")
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel> = emptyMap()
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory> = emptyMap()
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> = emptyMap()
}
