package org.mpc.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import org.mpc.data.portal.portalLoginDestination
import org.mpc.domain.model.PortalShortcutDestination
import org.mpc.domain.session.PortalNeed
import org.mpc.domain.session.PortalSignIn
import org.mpc.domain.session.PortalStatus
import org.mpc.presentation.viewModel.CampusSessionViewModel

@Composable
internal fun PortalSessionPanel(
    onOpenDestination: (PortalShortcutDestination) -> Unit,
    viewModel: CampusSessionViewModel = metroViewModel(),
) {
    val status by viewModel.portal.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel) {
        viewModel.refreshPortal()
    }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPortal()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Portal", style = MaterialTheme.typography.titleMedium)
            Text(status.label())
            if (status == PortalStatus.Blocked(PortalNeed.DISPLACED)) {
                Text(
                    text = "重新登入後，其他地方的 Portal 會被登出",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == PortalStatus.Live) {
                    TextButton(onClick = viewModel::signOut) { Text("登出") }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                when (val signIn = viewModel.signIn()) {
                                    is PortalSignIn.OpenBrowser -> onOpenDestination(portalLoginDestination(signIn.url))
                                }
                            }
                        },
                    ) {
                        Text("登入")
                    }
                }
                if (status is PortalStatus.Unavailable) {
                    TextButton(onClick = viewModel::refreshPortal) { Text("重試") }
                }
            }
        }
    }
}

private fun PortalStatus.label(): String = when (this) {
    PortalStatus.Unknown -> "正在確認登入狀態"

    PortalStatus.Live -> "已登入 Portal"

    PortalStatus.SignedOut -> "尚未登入 Portal"

    is PortalStatus.Unavailable -> "目前無法確認登入狀態"

    is PortalStatus.Blocked -> when (need) {
        PortalNeed.DISPLACED -> "Portal 已在其他地方登入"

        PortalNeed.USER_SIGNED_OUT -> "已登出 Portal"

        PortalNeed.NEEDS_PORTAL_PAGE -> "請在 Portal 頁面完成登入"

        PortalNeed.MANUAL_SIGN_IN,
        PortalNeed.COOLDOWN,
        PortalNeed.NO_CREDENTIALS,
        -> "尚未登入 Portal"
    }
}
