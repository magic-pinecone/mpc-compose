package org.mpc.bridge

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.mpc.domain.model.CourseSearchRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class CourseSearchBridgeTest {
    @Test
    fun catalogReplaysLatestSnapshotAfterObservationRestarts() = runTest {
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

    @Test
    fun searchSubmittedBeforeCollectionIsDeliveredInOrder() = runTest {
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
