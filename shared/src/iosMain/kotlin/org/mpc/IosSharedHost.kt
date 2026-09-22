package org.mpc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.mpc.presentation.CourseCatalogViewBinding
import org.mpc.presentation.CoursePlanningTimetableViewBinding
import org.mpc.presentation.SettingsScreen
import org.mpc.presentation.theme.MpcTheme
import org.mpc.presentation.viewModel.AppSettingsViewModel
import platform.UIKit.UIViewController

class IosSharedHost internal constructor(
    private val appGraph: AppGraph,
) {
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

    fun settingsScreenController(): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            val settingsViewModel: AppSettingsViewModel = metroViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            MpcTheme(themeMode = settings.themeMode) {
                SettingsScreen(
                    settings = settings,
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
