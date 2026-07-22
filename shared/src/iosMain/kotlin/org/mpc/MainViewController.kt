package org.mpc

import androidx.compose.ui.window.ComposeUIViewController

@Suppress("FunctionNaming", "UnusedVariable")
fun MainViewController() =
    ComposeUIViewController {
        val iosSharedHost = createIosSharedHost()
    }
