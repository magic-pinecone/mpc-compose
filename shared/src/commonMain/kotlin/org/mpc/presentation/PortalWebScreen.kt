package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.portal.isEeclassBrowserFlowUrl
import org.mpc.domain.portal.isEeclassFlowStartUrl
import org.mpc.domain.portal.isEeclassJavascriptAllowed
import org.mpc.presentation.viewModel.EeclassSessionViewModel

@Composable
fun PortalWebScreen(
    url: String,
) {
    val sessionViewModel: EeclassSessionViewModel = metroViewModel()
    val isEeclassFlow = remember(url) { isEeclassFlowStartUrl(url) }
    val webViewState = rememberWebViewState(url = url)
    val pageUrl = webViewState.lastLoadedUrl ?: url
    val isLoading = webViewState.isLoading
    val javascriptEnabled = isEeclassJavascriptAllowed(url)
    if (webViewState.settings.javaScriptEnabled != javascriptEnabled) {
        webViewState.settings.javaScriptEnabled = javascriptEnabled
    }

    LaunchedEffect(isEeclassFlow, isLoading, pageUrl) {
        if (!isEeclassFlow || !isEeclassBrowserFlowUrl(pageUrl)) return@LaunchedEffect
        if (isLoading) {
            sessionViewModel.browserNavigationStarted()
            return@LaunchedEffect
        }

        sessionViewModel.browserNavigationFinished(pageUrl)
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
    )
}
