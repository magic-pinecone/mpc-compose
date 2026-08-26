package org.mpc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.mpc.di.AppGraph
import org.mpc.navigation.CourseSelectionRoot
import org.mpc.presentation.CourseSelectionSearchScreen

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
    val backStack = rememberNavBackStack(CourseSelectionRoot)

    ProvideAppDependencies(appGraph) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Magic Pinecone")
                        },
                    )
                },
            ) { paddingValues ->
                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                        entryProvider =
                        entryProvider {
                            entry<CourseSelectionRoot> {
                                CourseSelectionSearchScreen(modifier = Modifier.fillMaxSize())
                            }
                        },
                    )
                }
            }
        }
    }
}
