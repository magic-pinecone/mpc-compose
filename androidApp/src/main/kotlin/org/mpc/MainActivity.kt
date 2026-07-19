package org.mpc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.mpc.core.createDataStore
import org.mpc.di.createAppGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val storage = createDataStore(applicationContext)
        val appGraph = createAppGraph(storage)

        setContent {
            AndroidAppShell(appGraph)
        }
    }
}
