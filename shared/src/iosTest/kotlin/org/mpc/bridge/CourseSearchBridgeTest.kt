package org.mpc.bridge

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.mpc.domain.model.CourseSearchRequest
import kotlin.test.assertEquals

val courseSearchBridgeTests by testSuite {
    test("catalog replays the latest snapshot after observation restarts") {
        runTest {
            val bridge = CourseSearchBridge()
            val received = mutableListOf<CourseCatalogSnapshot>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                bridge.catalogSnapshot.collect { received.add(it) }
            }
            val loaded = CourseCatalogSnapshot(isLoading = false)
            bridge.updateCatalogSnapshot(loaded)
            testScheduler.runCurrent()
            bridge.updateCatalogSnapshot(loaded.copy())
            testScheduler.runCurrent()
            assertEquals(listOf(CourseCatalogSnapshot(), loaded), received)

            collector.cancelAndJoin()
            val failed = loaded.copy(failureMessage = "offline")
            bridge.updateCatalogSnapshot(failed)
            testScheduler.runCurrent()
            assertEquals(listOf(CourseCatalogSnapshot(), loaded), received)

            val restartedCollector = launch(start = CoroutineStart.UNDISPATCHED) {
                bridge.catalogSnapshot.collect { received.add(it) }
            }
            assertEquals(listOf(CourseCatalogSnapshot(), loaded, failed), received)
            restartedCollector.cancelAndJoin()
        }
    }

    test("search submitted before collection is delivered in order") {
        runTest {
            val bridge = CourseSearchBridge()
            val requests = listOf(
                CourseSearchRequest(semester = "115-1", query = "math"),
                CourseSearchRequest(semester = "115-1", query = ""),
            )
            requests.forEach(bridge::submitSearch)
            assertEquals(requests[0], bridge.sendRequests.first())
            assertEquals(requests[1], bridge.sendRequests.first())
        }
    }
}
