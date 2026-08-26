package org.mpc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import org.mpc.di.AppGraph
import org.mpc.navigation.AndroidNavigator
import org.mpc.navigation.AppRoot
import org.mpc.navigation.CoursePlanningRoot
import org.mpc.navigation.HomeRoot
import org.mpc.navigation.NewsRoot
import org.mpc.navigation.PortalRoot
import org.mpc.navigation.SettingsRoute
import org.mpc.navigation.TopLevelRoute
import org.mpc.navigation.rememberAndroidNavigationState
import org.mpc.presentation.CourseSelectionSearchScreen

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
    val appBackStack = rememberNavBackStack(AppRoot)
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavKey>() }
    val entryProvider =
        entryProvider<NavKey> {
            entry<AppRoot> {
                AndroidPrimaryNavigation(
                    onOpenSettings = {
                        if (SettingsRoute !in appBackStack) {
                            appBackStack.add(SettingsRoute)
                        }
                    },
                )
            }
            if (isExpanded) {
                entry<SettingsRoute>(
                    metadata =
                    DialogSceneStrategy.dialog(
                        DialogProperties(windowTitle = "設定"),
                    ),
                ) {
                    SettingsScreen(
                        modifier =
                        Modifier
                            .widthIn(max = 560.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        onClose = { appBackStack.removeLastOrNull() },
                    )
                }
            } else {
                entry<SettingsRoute> {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClose = { appBackStack.removeLastOrNull() },
                    )
                }
            }
        }

    ProvideAppDependencies(appGraph) {
        MaterialTheme {
            NavDisplay(
                backStack = appBackStack,
                onBack = { appBackStack.removeLastOrNull() },
                entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sceneStrategies = listOf(dialogSceneStrategy),
                entryProvider = entryProvider,
            )
        }
    }
}

@Composable
private fun AndroidPrimaryNavigation(onOpenSettings: () -> Unit) {
    val navigationState = rememberAndroidNavigationState()
    val navigator = remember(navigationState) { AndroidNavigator(navigationState) }
    val entryProvider =
        entryProvider<NavKey> {
            entry<HomeRoot> {
                TopLevelPlaceholder(title = "首頁")
            }
            entry<NewsRoot> {
                TopLevelPlaceholder(title = "新聞")
            }
            entry<PortalRoot> {
                TopLevelPlaceholder(title = "Portal")
            }
            entry<CoursePlanningRoot> {
                CourseSelectionSearchScreen(modifier = Modifier.fillMaxSize())
            }
        }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            topLevelNavigationItems.forEach { item ->
                item(
                    selected = navigationState.selectedTopLevelRoute == item.route,
                    onClick = { navigator.navigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                        )
                    },
                    label = { Text(item.label) },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Magic Pinecone")
                    },
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "設定",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            NavDisplay(
                entries = navigationState.toDecoratedEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    onClose: () -> Unit,
) {
    Surface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "關閉設定",
                        )
                    }
                },
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("設定")
            }
        }
    }
}

@Composable
private fun TopLevelPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(title)
    }
}

private data class TopLevelNavigationItem(
    val route: TopLevelRoute,
    val label: String,
    val icon: ImageVector,
)

private val topLevelNavigationItems =
    listOf(
        TopLevelNavigationItem(HomeRoot, "首頁", Icons.Default.Home),
        TopLevelNavigationItem(NewsRoot, "新聞", Icons.Default.Info),
        TopLevelNavigationItem(PortalRoot, "Portal", Icons.Default.AccountCircle),
        TopLevelNavigationItem(CoursePlanningRoot, "選課", Icons.Default.DateRange),
    )
