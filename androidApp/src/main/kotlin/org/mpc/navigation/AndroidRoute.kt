package org.mpc.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AndroidRoute : NavKey

@Serializable
data object CourseSelectionRoot : AndroidRoute
