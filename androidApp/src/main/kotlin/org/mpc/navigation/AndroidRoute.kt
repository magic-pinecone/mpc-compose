package org.mpc.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface AndroidRoute : NavKey

@Serializable
sealed interface TopLevelRoute : AndroidRoute

@Serializable
data object HomeRoot : TopLevelRoute

@Serializable
data object NewsRoot : TopLevelRoute

@Serializable
data object PortalRoot : TopLevelRoute

@Serializable
data object CoursePlanningRoot : TopLevelRoute

val topLevelRoutes: Set<TopLevelRoute> =
    linkedSetOf(
        HomeRoot,
        NewsRoot,
        PortalRoot,
        CoursePlanningRoot,
    )
