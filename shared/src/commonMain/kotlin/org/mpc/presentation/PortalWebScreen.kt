package org.mpc.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.domain.session.CampusSites
import org.mpc.presentation.viewModel.CampusSessionViewModel

@Composable
fun PortalWebScreen(
    url: String,
) {
    val sessionViewModel: CampusSessionViewModel = metroViewModel()
    val webViewState = rememberWebViewState(url = url)
    val pageUrl = webViewState.lastLoadedUrl ?: url
    val isLoading = webViewState.isLoading
    val javascriptEnabled = CampusSites.allowsJavascript(url)
    if (webViewState.settings.javaScriptEnabled != javascriptEnabled) {
        webViewState.settings.javaScriptEnabled = javascriptEnabled
    }

    LaunchedEffect(isLoading, pageUrl) {
        sessionViewModel.browserPage(pageUrl, isLoading)
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
    )
}
