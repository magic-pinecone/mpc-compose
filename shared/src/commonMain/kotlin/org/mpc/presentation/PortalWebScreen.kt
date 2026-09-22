package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewState

@Composable
fun PortalWebScreen(
    url: String,
) {
    val webViewState = rememberWebViewState(url = url)

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
    )
}
