package org.mpc

import androidx.compose.ui.window.ComposeUIViewController
import org.mpc.bridge.CourseSearchBridge
import org.mpc.core.createDataStore
import org.mpc.di.AppGraph
import org.mpc.di.createAppGraph
import org.mpc.presentation.CourseSearchResultViewBinding
import platform.UIKit.UIViewController

class IosSharedHost internal constructor(
    private val appGraph: AppGraph
){
    fun courseSearchScreenController(bridge: CourseSearchBridge): UIViewController = ComposeUIViewController {
        ProvideAppDependencies(appGraph) {
            CourseSearchResultViewBinding(bridge)
        }
    }
}

fun createIosSharedHost(): IosSharedHost {
    val storage = createDataStore()
    return IosSharedHost(createAppGraph(storage))
}