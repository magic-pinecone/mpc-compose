package org.mpc.di

import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import org.mpc.domain.repository.CourseRepository

@DependencyGraph(AppScope::class)
interface AppGraph: ViewModelGraph {
    val courseRepository: CourseRepository
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides storage: Storage<Preferences>
        ): AppGraph
    }

}

fun createAppGraph(storage: Storage<Preferences>): AppGraph = createGraphFactory<AppGraph.Factory>().create(storage)