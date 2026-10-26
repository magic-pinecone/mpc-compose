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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import org.mpc.domain.session.CampusSites
import org.mpc.domain.session.Renewal
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteStatus
import org.mpc.presentation.viewModel.CampusSessionViewModel

private val TIMETABLE_PATH = SitePath.of("/dashboard/myTimeTable")

@Composable
internal fun EeclassSessionPanel(
    onOpenDestination: (PortalShortcutDestination) -> Unit,
    viewModel: CampusSessionViewModel = metroViewModel(),
) {
    val site = CampusSites.Eeclass
    val state by viewModel.status(site).collectAsStateWithLifecycle()
    // The page the user asked for before signing in; opened once they are back.
    var pendingTaskPath by rememberSaveable { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel) {
        viewModel.refresh(site)
    }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh(site)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(site.displayName)
            Text(state.statusLabel())
            if (state == SiteStatus.Checking) {
                CircularProgressIndicator()
            }
            val pendingPath = pendingTaskPath?.let(SitePath::parse)
            if (state == SiteStatus.SignedIn && pendingPath != null) {
                Button(
                    onClick = {
                        pendingTaskPath = null
                        onOpenDestination(eeclassTaskDestination(site.url(pendingPath)))
                    },
                ) {
                    Text("繼續查看課表")
                }
            }
            if (state == SiteStatus.SignedIn) {
                Text("登出請使用網站帳號選單")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (state == SiteStatus.SignedIn) {
                            onOpenDestination(eeclassTaskDestination(viewModel.browserEntry(site, TIMETABLE_PATH)))
                        } else {
                            pendingTaskPath = TIMETABLE_PATH.value
                            onOpenDestination(eeclassPortalLaunchDestination())
                        }
                    },
                ) {
                    Text("查看課表")
                }
                when (val current = state) {
                    SiteStatus.SignedIn -> TextButton(
                        onClick = { onOpenDestination(eeclassDashboardDestination()) },
                    ) {
                        Text("開啟 ee-class")
                    }

                    is SiteStatus.SignedOut -> if (current.renewal == Renewal.HANDOFF) {
                        TextButton(onClick = { viewModel.reconnect(site) }) { Text("重新連線") }
                    } else {
                        TextButton(onClick = { onOpenDestination(eeclassPortalLaunchDestination()) }) { Text("登入") }
                    }

                    is SiteStatus.Unavailable -> {
                        TextButton(onClick = { viewModel.refresh(site) }) { Text("重試") }
                        TextButton(onClick = { onOpenDestination(eeclassPortalLaunchDestination()) }) { Text("登入") }
                    }

                    SiteStatus.Unknown,
                    SiteStatus.Checking,
                    -> Unit
                }
            }
        }
    }
}

private fun SiteStatus.statusLabel(): String = when (this) {
    SiteStatus.Unknown -> "尚未確認登入狀態"

    SiteStatus.Checking -> "正在確認登入狀態"

    is SiteStatus.SignedOut -> when (renewal) {
        Renewal.HANDOFF -> "ee-class 已登出，可透過 Portal 重新連線"

        Renewal.PORTAL_SIGN_IN_UNATTENDED,
        Renewal.PORTAL_SIGN_IN_BY_USER,
        -> "尚未登入 ee-class"
    }

    SiteStatus.SignedIn -> "已登入 ee-class"

    is SiteStatus.Unavailable -> "目前無法確認登入狀態"
}
