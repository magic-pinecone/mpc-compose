package org.mpc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.di.AppGraph
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.PortalLaunchMode
import org.mpc.domain.repository.CourseRepository
import org.mpc.navigation.AndroidNavigator
import org.mpc.navigation.AppRoot
import org.mpc.navigation.CoursePlanningRoot
import org.mpc.navigation.HomeRoot
import org.mpc.navigation.NewsRoot
import org.mpc.navigation.PortalRoot
import org.mpc.navigation.PortalWebRoute
import org.mpc.navigation.SettingsRoute
import org.mpc.navigation.TopLevelRoute
import org.mpc.navigation.rememberAndroidNavigationState
import org.mpc.presentation.CoursePlanningScreen
import org.mpc.presentation.CoursePlanningView
import org.mpc.presentation.PortalScreen
import org.mpc.presentation.PortalWebScreen
import org.mpc.presentation.SettingsScreen
import org.mpc.presentation.theme.AndroidMpcTheme
import org.mpc.presentation.viewModel.AppSettingsViewModel
import org.mpc.presentation.views.coursePlanning.SelectedCoursesBottomSheet

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
    ProvideAppDependencies(appGraph) {
        AndroidAppContent(appGraph)
    }
}

@Composable
private fun AndroidAppContent(appGraph: AppGraph) {
    val settingsViewModel: AppSettingsViewModel = metroViewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
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
                SettingsDialog(
                    settingsViewModel = settingsViewModel,
                    onClose = { appBackStack.removeLastOrNull() },
                )
            }
        }

    AndroidMpcTheme(themeMode = settings.themeMode) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AndroidPrimaryNavigation(
    courseRepository: CourseRepository,
    onOpenSettings: () -> Unit,
) {
    val navigationState = rememberAndroidNavigationState()
    val navigator = remember(navigationState) { AndroidNavigator(navigationState) }
    val uriHandler = LocalUriHandler.current
    var selectedCoursePlanningView by rememberSaveable { mutableStateOf(CoursePlanningView.CATALOG) }
    var selectedCourses by remember { mutableStateOf(emptyList<CourseSummary>()) }
    var isShowingSelectedCourses by rememberSaveable { mutableStateOf(false) }
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val entryProvider =
        entryProvider<NavKey> {
            entry<HomeRoot> {
                TopLevelPlaceholder(title = "首頁")
            }
            entry<NewsRoot> {
                TopLevelPlaceholder(title = "新聞")
            }
            entry<PortalRoot> {
                PortalScreen(
                    modifier = Modifier.fillMaxSize(),
                    onOpenDestination = { destination ->
                        when (destination.launchMode) {
                            PortalLaunchMode.IN_APP -> {
                                navigator.navigate(
                                    PortalWebRoute(
                                        title = destination.title,
                                        url = destination.url,
                                    ),
                                )
                            }

                            PortalLaunchMode.EXTERNAL -> uriHandler.openUri(destination.url)
                        }
                    },
                )
            }
            entry<PortalWebRoute> { route ->
                PortalWebScreen(
                    url = route.url,
                )
            }
            entry<CoursePlanningRoot> {
                CoursePlanningScreen(
                    modifier = Modifier.fillMaxSize(),
                    selectedView = selectedCoursePlanningView,
                    onSelectedViewChange = { view -> selectedCoursePlanningView = view },
                    courseRepository = courseRepository,
                    onSelectedCoursesChange = { courses -> selectedCourses = courses },
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
        val currentPortalWebRoute = navigationState.currentBackStack.lastOrNull() as? PortalWebRoute
        val topBarState =
            PrimaryTopBarState(
                currentPortalWebRoute = currentPortalWebRoute,
                selectedTopLevelRoute = navigationState.selectedTopLevelRoute,
                isExpanded = isExpanded,
                selectedCoursePlanningView = selectedCoursePlanningView,
                selectedCourseCount = selectedCourses.size,
            )

        Scaffold(
            topBar = {
                AndroidPrimaryTopBar(
                    state = topBarState,
                    onBack = { navigator.goBack() },
                    onOpenSelectedCourses = { isShowingSelectedCourses = true },
                    onOpenSettings = onOpenSettings,
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                if (isShowingSelectedCourses) {
                    SelectedCoursesBottomSheet(
                        courses = selectedCourses,
                        onDismissRequest = { isShowingSelectedCourses = false },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AndroidPrimaryTopBar(
    state: PrimaryTopBarState,
    onBack: () -> Unit,
    onOpenSelectedCourses: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text(state.title) },
        navigationIcon = {
            if (state.currentPortalWebRoute != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                    )
                }
            }
        },
        actions = {
            if (state.currentPortalWebRoute == null) {
                if (state.showsCoursePlanAction) {
                    BadgedBox(
                        badge = {
                            if (state.selectedCourseCount > 0) {
                                Badge {
                                    Text(
                                        state.selectedCourseCount
                                            .coerceAtMost(MAX_COURSE_COUNT_BADGE)
                                            .toString(),
                                    )
                                }
                            }
                        },
                    ) {
                        FilledTonalIconButton(onClick = onOpenSelectedCourses) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "查看已選課程",
                            )
                        }
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "設定",
                    )
                }
            }
        },
    )
}

private data class PrimaryTopBarState(
    val currentPortalWebRoute: PortalWebRoute?,
    val selectedTopLevelRoute: NavKey,
    val isExpanded: Boolean,
    val selectedCoursePlanningView: CoursePlanningView,
    val selectedCourseCount: Int,
) {
    val title: String
        get() =
            when {
                currentPortalWebRoute != null -> currentPortalWebRoute.title
                selectedTopLevelRoute == CoursePlanningRoot && isExpanded -> "選課"
                selectedTopLevelRoute == CoursePlanningRoot -> selectedCoursePlanningView.title
                selectedTopLevelRoute == HomeRoot -> "首頁"
                selectedTopLevelRoute == NewsRoot -> "新聞"
                selectedTopLevelRoute == PortalRoot -> "Portal"
                else -> "Magic Pinecone"
            }

    val showsCoursePlanAction: Boolean
        get() =
            selectedTopLevelRoute == CoursePlanningRoot
}

private const val MAX_COURSE_COUNT_BADGE = 99

@Composable
private fun SettingsDialog(
    settingsViewModel: AppSettingsViewModel,
    onClose: () -> Unit,
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val settingsWriteFailed by settingsViewModel.settingsWriteFailed.collectAsStateWithLifecycle()
    val isExpanded =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val surfaceModifier =
        if (isExpanded) {
            Modifier
                .widthIn(max = 560.dp)
                .clip(MaterialTheme.shapes.extraLarge)
        } else {
            Modifier.fillMaxSize()
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
            SettingsScreen(
                settings = settings,
                settingsWriteFailed = settingsWriteFailed,
                modifier = Modifier.fillMaxSize(),
                onThemeModeSelected = settingsViewModel::setThemeMode,
                onPortalAuthenticationModeSelected = settingsViewModel::setPortalAuthenticationMode,
            )
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
