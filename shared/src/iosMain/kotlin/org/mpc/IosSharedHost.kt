package org.mpc

import androidx.compose.ui.window.ComposeUIViewController
import org.mpc.bridge.CoursePlanBridge
import org.mpc.bridge.CourseSearchBridge
import org.mpc.core.createDataStore
import org.mpc.core.createDatabase
import org.mpc.core.createDatabaseBuilder
import org.mpc.di.AppGraph
import org.mpc.di.createAppGraph
import org.mpc.presentation.CourseSearchResultViewBinding
import org.mpc.presentation.CourseSelectionTimetableViewBinding
import platform.UIKit.UIViewController

class IosSharedHost internal constructor(
    private val appGraph: AppGraph,
) {
    fun courseSearchScreenController(
        bridge: CourseSearchBridge,
        planBridge: CoursePlanBridge,
    ): UIViewController =
        ComposeUIViewController {
            ProvideAppDependencies(appGraph) {
                CourseSearchResultViewBinding(bridge, planBridge)
            }
        }

    fun courseSelectionTimetableScreenController(planBridge: CoursePlanBridge): UIViewController =
        ComposeUIViewController {
            ProvideAppDependencies(appGraph) {
                CourseSelectionTimetableViewBinding(planBridge)
            }
        }
}

fun createIosSharedHost(): IosSharedHost {
    val storage = createDataStore()
    val database =
        createDatabase(
            createDatabaseBuilder(),
        )
    return IosSharedHost(createAppGraph(storage, database))
}
