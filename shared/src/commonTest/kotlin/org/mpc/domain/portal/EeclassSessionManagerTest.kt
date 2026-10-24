package org.mpc.domain.portal

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeEeclassSessionChecker(
    private val results: ArrayDeque<EeclassSessionCheck>,
    private val gate: CompletableDeferred<Unit>? = null,
) : EeclassSessionChecker {
    override suspend fun check(): EeclassSessionCheck {
        gate?.await()
        return results.removeFirst()
    }
}

val eeclassSessionManagerTests by testSuite {
    test("task intent remains pending until its authenticated page finishes") {
        runTest {
            val manager = DefaultEeclassSessionManager(
                FakeEeclassSessionChecker(
                    ArrayDeque(
                        listOf(
                            EeclassSessionCheck.SignedOut,
                            EeclassSessionCheck.Authenticated,
                            EeclassSessionCheck.Authenticated,
                        ),
                    ),
                ),
            )
            manager.requestTask(EECLASS_TIMETABLE_PATH)

            assertNull(manager.pendingTaskUrlIfAuthenticated())
            manager.restore()
            assertEquals(EECLASS_TIMETABLE_PATH, manager.pendingTaskPath.value)
            manager.restore()

            val taskUrl = "$EECLASS_BASE_URL$EECLASS_TIMETABLE_PATH"
            assertEquals(taskUrl, manager.pendingTaskUrlIfAuthenticated())
            assertEquals(EECLASS_TIMETABLE_PATH, manager.pendingTaskPath.value)
            manager.browserNavigationFinished(taskUrl)
            assertNull(manager.pendingTaskPath.value)
        }
    }

    test("an expired task launch keeps its path through sign in") {
        runTest {
            val manager = DefaultEeclassSessionManager(
                FakeEeclassSessionChecker(
                    ArrayDeque(
                        listOf(
                            EeclassSessionCheck.Authenticated,
                            EeclassSessionCheck.SignedOut,
                            EeclassSessionCheck.Authenticated,
                            EeclassSessionCheck.Authenticated,
                        ),
                    ),
                ),
            )
            val taskUrl = "$EECLASS_BASE_URL$EECLASS_TIMETABLE_PATH"
            manager.restore()
            manager.requestTask(EECLASS_TIMETABLE_PATH)

            assertEquals(taskUrl, manager.pendingTaskUrlIfAuthenticated())
            manager.browserNavigationFinished("$EECLASS_BASE_URL/index/login?next=%2Fdashboard%2FmyTimeTable")
            assertEquals(EeclassSessionState.SignedOut, manager.state.value)
            assertEquals(EECLASS_TIMETABLE_PATH, manager.pendingTaskPath.value)

            manager.restore()
            assertEquals(taskUrl, manager.pendingTaskUrlIfAuthenticated())
            manager.browserNavigationFinished(taskUrl)
            assertNull(manager.pendingTaskPath.value)
        }
    }

    test("ee-class web flow keeps the trusted Portal launcher scoped") {
        assertTrue(isEeclassFlowStartUrl("https://portal.ncu.edu.tw/system/ncueeclass"))
        assertTrue(isEeclassBrowserFlowUrl("https://portal.ncu.edu.tw/login?next=%2Fgo"))
        assertFalse(isEeclassFlowStartUrl("https://portal.ncu.edu.tw/system/129"))
        assertFalse(isEeclassBrowserFlowUrl("https://portal.ncu.edu.tw.evil.example/system/ncueeclass"))
    }

    test("cancelling a check restores the previous session state") {
        runTest {
            val gate = CompletableDeferred<Unit>()
            val checker = object : EeclassSessionChecker {
                var calls = 0
                override suspend fun check(): EeclassSessionCheck {
                    calls++
                    if (calls == 1) return EeclassSessionCheck.Authenticated
                    gate.await()
                    return EeclassSessionCheck.SignedOut
                }
            }
            val manager = DefaultEeclassSessionManager(checker)
            manager.restore()

            val job = launch { manager.restore() }
            yield()
            assertEquals(EeclassSessionState.Checking, manager.state.value)
            job.cancelAndJoin()

            assertEquals(EeclassSessionState.Authenticated, manager.state.value)
        }
    }
}
