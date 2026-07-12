package org.mpc

import androidx.compose.ui.window.ComposeUIViewController
import org.mpc.di.createAppGraph

fun MainViewController() = ComposeUIViewController {
    val appGraph = createAppGraph()
    App()
}