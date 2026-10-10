package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.mpc.domain.model.CourseSearchRequest
import org.mpc.domain.model.CourseSummary

class CourseSearchBridge {
    private val requests = Channel<CourseSearchRequest>(Channel.BUFFERED)
    internal val sendRequests = requests.receiveAsFlow()
    private var latestCatalogSnapshot = CourseCatalogSnapshot()
    private var catalogSnapshotObserver: ((CourseCatalogSnapshot) -> Unit)? = null

    fun submitSearch(request: CourseSearchRequest) {
        requests.trySend(request)
    }

    fun observeCatalogSnapshot(observer: ((CourseCatalogSnapshot) -> Unit)?) {
        catalogSnapshotObserver = observer
        observer?.invoke(latestCatalogSnapshot)
    }

    fun updateCatalogSnapshot(snapshot: CourseCatalogSnapshot) {
        if (latestCatalogSnapshot == snapshot) return

        latestCatalogSnapshot = snapshot
        catalogSnapshotObserver?.invoke(snapshot)
    }
}

data class CourseCatalogSnapshot(
    val semester: String = "115-1",
    val courses: List<CourseSummary> = emptyList(),
    val isLoading: Boolean = true,
    val failureMessage: String? = null,
)
