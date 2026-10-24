package org.mpc.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.data.portal.eeclassDashboardDestination
import org.mpc.data.portal.eeclassPortalLaunchDestination
import org.mpc.data.portal.eeclassTaskDestination
import org.mpc.domain.model.PortalShortcutDestination
import org.mpc.domain.portal.EECLASS_TIMETABLE_PATH
import org.mpc.domain.portal.EeclassSessionState
import org.mpc.presentation.viewModel.EeclassSessionViewModel

@Composable
internal fun EeclassSessionPanel(
    onOpenDestination: (PortalShortcutDestination) -> Unit,
    viewModel: EeclassSessionViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingTaskPath by viewModel.pendingTaskPath.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel) {
        viewModel.restore()
    }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.restore()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("新 ee-class")
            Text(state.statusLabel())
            if (state == EeclassSessionState.Checking) {
                CircularProgressIndicator()
            }
            if (state == EeclassSessionState.Authenticated && pendingTaskPath != null) {
                Button(
                    onClick = {
                        viewModel.pendingTaskUrlIfAuthenticated()?.let { url ->
                            onOpenDestination(eeclassTaskDestination(url))
                        }
                    },
                ) {
                    Text("繼續查看課表")
                }
            }
            if (state == EeclassSessionState.Authenticated) {
                Text("登出請使用網站帳號選單")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        viewModel.requestTask(EECLASS_TIMETABLE_PATH)
                        val taskUrl = viewModel.pendingTaskUrlIfAuthenticated()
                        if (taskUrl == null) {
                            onOpenDestination(eeclassPortalLaunchDestination())
                        } else {
                            onOpenDestination(eeclassTaskDestination(taskUrl))
                        }
                    },
                ) {
                    Text("查看課表")
                }
                when (state) {
                    EeclassSessionState.Authenticated -> TextButton(
                        onClick = { onOpenDestination(eeclassDashboardDestination()) },
                    ) {
                        Text("開啟 ee-class")
                    }

                    EeclassSessionState.SignedOut -> TextButton(
                        onClick = { onOpenDestination(eeclassPortalLaunchDestination()) },
                    ) {
                        Text("登入")
                    }

                    is EeclassSessionState.Unavailable -> {
                        TextButton(onClick = viewModel::restore) { Text("重試") }
                        TextButton(onClick = { onOpenDestination(eeclassPortalLaunchDestination()) }) { Text("登入") }
                    }

                    EeclassSessionState.Unknown,
                    EeclassSessionState.Checking,
                    -> Unit
                }
            }
        }
    }
}

private fun EeclassSessionState.statusLabel(): String = when (this) {
    EeclassSessionState.Unknown -> "尚未確認登入狀態"
    EeclassSessionState.Checking -> "正在確認登入狀態"
    EeclassSessionState.SignedOut -> "尚未登入 ee-class"
    EeclassSessionState.Authenticated -> "已登入 ee-class"
    is EeclassSessionState.Unavailable -> "目前無法確認登入狀態"
}
