package org.mpc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mpc.bridge.CoursePlanBridge
import org.mpc.bridge.CourseSearchBridge
import org.mpc.core.createDataStore
import org.mpc.core.createDatabase
import org.mpc.core.createDatabaseBuilder
import org.mpc.di.AppGraph
import org.mpc.di.createAppGraph
import org.mpc.domain.html.HtmlParser
import org.mpc.domain.model.AppSettings
import org.mpc.domain.model.AppThemeMode
import org.mpc.domain.model.CourseDetail
import org.mpc.domain.model.PortalShortcutDestination
import org.mpc.presentation.CourseCatalogViewBinding
import org.mpc.presentation.CoursePlanningTimetableViewBinding
import org.mpc.presentation.PortalScreen
import org.mpc.presentation.PortalWebScreen
import org.mpc.presentation.SettingsScreen
import org.mpc.presentation.theme.MpcTheme
import org.mpc.presentation.viewModel.AppSettingsViewModel
import platform.UIKit.UIViewController

class IosSharedHost internal constructor(
    private val appGraph: AppGraph,
    settingsScope: CoroutineScope = MainScope(),
) {
    private val loadedSettings: StateFlow<AppSettings?> = appGraph.appSettingsRepository.settings.stateIn(
        scope = settingsScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )
    val themeMode: Flow<AppThemeMode> = loadedSettings
        .filterNotNull()
        .map { it.themeMode }
        .distinctUntilChanged()

    @Throws(Exception::class)
    suspend fun loadCourseDetail(
        semester: String,
        serialNumber: String,
    ): CourseDetail = appGraph.courseRepository.fetchCourseDetail(semester, serialNumber)

    fun courseCatalogScreenController(
        bridge: CourseSearchBridge,
        planBridge: CoursePlanBridge,
    ): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            ThemedContent {
                CourseCatalogViewBinding(bridge, planBridge)
            }
        }
    }

    fun coursePlanningTimetableScreenController(planBridge: CoursePlanBridge): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            ThemedContent {
                CoursePlanningTimetableViewBinding(
                    planBridge = planBridge,
                    draftStore = appGraph.coursePlanDraftStore,
                )
            }
        }
    }

    fun portalScreenController(
        onOpenDestination: (PortalShortcutDestination) -> Unit,
    ): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            ThemedContent {
                PortalScreen(
                    modifier = Modifier.fillMaxSize(),
                    onOpenDestination = onOpenDestination,
                )
            }
        }
    }

    fun portalWebScreenController(url: String): UIViewController = ComposeUIViewController(
        configure = {
            // WKWebView already scrolls focused HTML fields above the keyboard.
            onFocusBehavior = OnFocusBehavior.DoNothing
        },
    ) {
        ProvideAppDependencies(appGraph) {
            ThemedContent {
                PortalWebScreen(url = url)
            }
        }
    }

    fun settingsScreenController(): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            ThemedContent { settings ->
                val settingsViewModel: AppSettingsViewModel = metroViewModel()
                val settingsWriteFailed by settingsViewModel.settingsWriteFailed.collectAsStateWithLifecycle()

                SettingsScreen(
                    settings = settings,
                    settingsWriteFailed = settingsWriteFailed,
                    onTimetableColorModeSelected = settingsViewModel::setTimetableColorMode,
                    onThemeModeSelected = settingsViewModel::setThemeMode,
                    onPortalAuthenticationModeSelected = settingsViewModel::setPortalAuthenticationMode,
                )
            }
        }
    }

    @Composable
    private fun ThemedContent(content: @Composable (AppSettings) -> Unit) {
        val settingsState by loadedSettings.collectAsStateWithLifecycle()
        val settings = settingsState ?: return
        MpcTheme(
            themeMode = settings.themeMode,
            timetableColorMode = settings.timetableColorMode,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MpcTheme.colors.background,
            ) {
                content(settings)
            }
        }
    }
}

fun createIosSharedHost(htmlParser: HtmlParser): IosSharedHost {
    val storage = createDataStore()
    val dataStore = createDataStore(storage)
    val database =
        createDatabase(
            createDatabaseBuilder(),
        )
    return IosSharedHost(createAppGraph(dataStore, database, htmlParser))
}
