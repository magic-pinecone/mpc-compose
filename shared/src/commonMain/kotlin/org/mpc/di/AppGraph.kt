package org.mpc.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import org.mpc.data.local.database.AppDatabase
import org.mpc.domain.repository.CourseRepository

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    val courseRepository: CourseRepository

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides dataStore: DataStore<Preferences>,
            @Provides database: AppDatabase,
        ): AppGraph
    }
}

fun createAppGraph(
    dataStore: DataStore<Preferences>,
    database: AppDatabase,
): AppGraph = createGraphFactory<AppGraph.Factory>().create(dataStore, database)
