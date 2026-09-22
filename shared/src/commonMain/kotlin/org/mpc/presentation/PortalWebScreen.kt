package org.mpc.presentation

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewState

@Composable
fun PortalWebScreen(
    title: String,
    url: String,
    onClose: () -> Unit,
) {
    val webViewState = rememberWebViewState(url = url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                // The app shell already consumes the system top inset. This screen is nested
                // inside that shell, so applying it again leaves a blank band above the toolbar.
                windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        WebView(
            state = webViewState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}
