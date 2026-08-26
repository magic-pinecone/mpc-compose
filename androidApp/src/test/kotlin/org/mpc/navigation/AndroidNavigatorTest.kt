package org.mpc.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AndroidNavigatorTest {
    private lateinit var state: AndroidNavigationState
    private lateinit var navigator: AndroidNavigator

    @Before
    fun setUp() {
        state =
            AndroidNavigationState(
                startRoute = HomeRoot,
                selectedTopLevelRoute = mutableStateOf<NavKey>(HomeRoot),
                backStacks =
                topLevelRoutes.associateWith { route ->
                    NavBackStack<NavKey>(route)
                },
            )
        navigator = AndroidNavigator(state)
    }

    @Test
    fun switchingTopLevelRoutesPreservesTheirStacks() {
        navigator.navigate(CoursePlanningRoot)
        navigator.navigate(TestDetail)
        navigator.navigate(NewsRoot)
        navigator.navigate(CoursePlanningRoot)

        assertEquals(listOf(CoursePlanningRoot, TestDetail), state.currentBackStack)
    }

    @Test
    fun reselectingTopLevelRoutePopsItsStackToRoot() {
        navigator.navigate(CoursePlanningRoot)
        navigator.navigate(TestDetail)

        navigator.navigate(CoursePlanningRoot)

        assertEquals(listOf(CoursePlanningRoot), state.currentBackStack)
    }

    @Test
    fun backPopsCurrentStackBeforeReturningHome() {
        navigator.navigate(CoursePlanningRoot)
        navigator.navigate(TestDetail)

        assertTrue(navigator.goBack())
        assertEquals(CoursePlanningRoot, state.selectedTopLevelRoute)
        assertEquals(listOf(CoursePlanningRoot), state.currentBackStack)

        assertTrue(navigator.goBack())
        assertEquals(HomeRoot, state.selectedTopLevelRoute)
    }

    @Test
    fun backAtHomeRootIsNotConsumed() {
        assertFalse(navigator.goBack())
    }

    @Serializable
    private data object TestDetail : AndroidRoute
}
