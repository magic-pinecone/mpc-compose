package org.mpc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.bridge.CoursePlanBridge
import org.mpc.bridge.CourseSearchBridge
import org.mpc.core.createDataStore
import org.mpc.core.createDatabase
import org.mpc.core.createDatabaseBuilder
import org.mpc.di.AppGraph
import org.mpc.di.createAppGraph
import org.mpc.domain.model.AppThemeMode
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
) {
    private var latestThemeMode = AppThemeMode.SYSTEM
    private var themeModeObserver: ((AppThemeMode) -> Unit)? = null

    fun observeThemeMode(observer: ((AppThemeMode) -> Unit)?) {
        themeModeObserver = observer
        observer?.invoke(latestThemeMode)
    }

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
                CoursePlanningTimetableViewBinding(planBridge)
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
            ThemedContent {
                val settingsViewModel: AppSettingsViewModel = metroViewModel()
                val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
                val settingsWriteFailed by settingsViewModel.settingsWriteFailed.collectAsStateWithLifecycle()

                SettingsScreen(
                    settings = settings,
                    settingsWriteFailed = settingsWriteFailed,
                    onThemeModeSelected = settingsViewModel::setThemeMode,
                    onPortalAuthenticationModeSelected = settingsViewModel::setPortalAuthenticationMode,
                )
            }
        }
    }

    @Composable
    private fun ThemedContent(content: @Composable () -> Unit) {
        val settingsViewModel: AppSettingsViewModel = metroViewModel()
        val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
        SideEffect {
            latestThemeMode = settings.themeMode
            themeModeObserver?.invoke(latestThemeMode)
        }

        MpcTheme(themeMode = settings.themeMode, content = content)
    }
}

fun createIosSharedHost(): IosSharedHost {
    val storage = createDataStore()
    val dataStore = createDataStore(storage)
    val database =
        createDatabase(
            createDatabaseBuilder(),
        )
    return IosSharedHost(createAppGraph(dataStore, database))
}
