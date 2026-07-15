package org.mpc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.mpc.di.AppGraph
import org.mpc.di.createAppGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appGraph = createAppGraph()

        setContent {
            AndroidApp(appGraph)
        }
    }
}

@Composable
fun AndroidApp(appGraph: AppGraph) {
    App(appGraph)
}

@Preview
@Composable
fun AppAndroidPreview() {
}