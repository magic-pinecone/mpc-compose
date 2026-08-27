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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
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
import org.mpc.domain.repository.CourseRepository
import org.mpc.navigation.AndroidNavigator
import org.mpc.navigation.AppRoot
import org.mpc.navigation.CourseDetailsRoute
import org.mpc.navigation.CoursePlanningRoot
import org.mpc.navigation.HomeRoot
import org.mpc.navigation.NewsRoot
import org.mpc.navigation.PortalRoot
import org.mpc.navigation.SettingsRoute
import org.mpc.navigation.TopLevelRoute
import org.mpc.navigation.rememberAndroidNavigationState
import org.mpc.navigation.scene.BottomSheetSceneStrategy
import org.mpc.presentation.CourseDetailsScreen
import org.mpc.presentation.CoursePlanningScreen

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
    val appBackStack = rememberNavBackStack(AppRoot)
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavKey>() }

    // Keep entry metadata independent of the window size. Navigation 3 caches metadata by the
    // back-stack key, while the scene-strategy list is re-evaluated when the window changes.
    val entryProvider =
        entryProvider<NavKey> {
            entry<AppRoot> {
                AndroidPrimaryNavigation(
                    courseRepository = appGraph.courseRepository,
                    onOpenSettings = {
                        if (SettingsRoute !in appBackStack) {
                            appBackStack.add(SettingsRoute)
                        }
                    },
                )
            }
            entry<SettingsRoute>(
                metadata =
                DialogSceneStrategy.dialog(
                    DialogProperties(windowTitle = "設定"),
                ),
            ) {
                SettingsScreen(
                    modifier = Modifier,
                    onClose = { appBackStack.removeLastOrNull() },
                )
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
                sceneStrategies =
                if (isExpanded) {
                    listOf(dialogSceneStrategy)
                } else {
                    emptyList()
                },
                entryProvider = entryProvider,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AndroidPrimaryNavigation(
    courseRepository: CourseRepository,
    onOpenSettings: () -> Unit,
) {
    val navigationState = rememberAndroidNavigationState()
    val navigator = remember(navigationState) { AndroidNavigator(navigationState) }
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val bottomSheetSceneStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavKey>() }
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
                CoursePlanningScreen(
                    modifier = Modifier.fillMaxSize(),
                    onCourseClick = { semester, course ->
                        navigationState.currentBackStack.removeAll { route ->
                            route is CourseDetailsRoute
                        }
                        navigator.navigate(
                            CourseDetailsRoute(
                                semester = semester,
                                serialNumber = course.serialNo.value,
                            ),
                        )
                    },
                )
            }
            entry<CourseDetailsRoute>(
                metadata =
                DialogSceneStrategy.dialog(
                    DialogProperties(windowTitle = "課程詳細資訊"),
                ) + BottomSheetSceneStrategy.bottomSheet(),
            ) { route ->
                CourseDetailsScreen(
                    route = route,
                    courseRepository = courseRepository,
                    onClose = { navigator.goBack() },
                )
            }
        }

    NavigationSuiteScaffold(
        navigationItems = {
            topLevelNavigationItems.forEach { item ->
                NavigationSuiteItem(
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
                sceneStrategies =
                if (isExpanded) {
                    listOf(dialogSceneStrategy)
                } else {
                    listOf(bottomSheetSceneStrategy)
                },
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
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val surfaceModifier =
        if (isExpanded) {
            modifier
                .widthIn(max = 560.dp)
                .clip(MaterialTheme.shapes.extraLarge)
        } else {
            modifier.fillMaxSize()
        }

    Surface(modifier = surfaceModifier) {
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
