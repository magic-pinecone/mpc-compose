package org.mpc.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import org.mpc.domain.repository.CourseRepository

@DependencyGraph(AppScope::class)
interface AppGraph {
    val courseRepository: CourseRepository
}

fun createAppGraph(): AppGraph = createGraph<AppGraph>()