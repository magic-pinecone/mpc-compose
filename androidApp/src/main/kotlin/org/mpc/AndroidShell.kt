package org.mpc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.mpc.di.AppGraph
import org.mpc.navigation.AndroidNavigator
import org.mpc.navigation.CoursePlanningRoot
import org.mpc.navigation.HomeRoot
import org.mpc.navigation.NewsRoot
import org.mpc.navigation.PortalRoot
import org.mpc.navigation.TopLevelRoute
import org.mpc.navigation.rememberAndroidNavigationState
import org.mpc.presentation.CourseSelectionSearchScreen

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
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

    ProvideAppDependencies(appGraph) {
        MaterialTheme {
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
