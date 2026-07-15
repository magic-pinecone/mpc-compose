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
import org.mpc.di.AppGraph

@Composable
fun AndroidAppShell(appGraph: AppGraph) {
    ProvideAppDependencies(appGraph) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Magic Pinecone")
                        }
                    )
                }
            ) {
                paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ){

                    }
            }
        }
    }
}