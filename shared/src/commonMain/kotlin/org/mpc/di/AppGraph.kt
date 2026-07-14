package org.mpc.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import org.mpc.domain.repository.CourseRepository

@DependencyGraph(AppScope::class)
interface AppGraph: ViewModelGraph {
    val courseRepository: CourseRepository
}

fun createAppGraph(): AppGraph = createGraph<AppGraph>()