package org.mpc.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface AndroidRoute : NavKey

@Serializable
data object AppRoot : AndroidRoute

@Serializable
data object SettingsRoute : AndroidRoute

@Serializable
sealed interface TopLevelRoute : AndroidRoute

@Serializable
data object HomeRoot : TopLevelRoute

@Serializable
data object NewsRoot : TopLevelRoute

@Serializable
data object PortalRoot : TopLevelRoute

@Serializable
data class PortalWebRoute(
    val title: String,
    val url: String,
) : AndroidRoute

@Serializable
data object CoursePlanningRoot : TopLevelRoute

@Serializable
data class CourseDetailsRoute(
    val semester: String,
    val serialNumber: String,
) : AndroidRoute

val topLevelRoutes: Set<TopLevelRoute> =
    linkedSetOf(
        HomeRoot,
        NewsRoot,
        PortalRoot,
        CoursePlanningRoot,
    )
