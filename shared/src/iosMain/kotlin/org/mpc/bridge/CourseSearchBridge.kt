package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.mpc.domain.model.CourseSearchRequest
import org.mpc.domain.model.CourseSummary

class CourseSearchBridge {
    private val requests = Channel<CourseSearchRequest>(Channel.BUFFERED)
    internal val sendRequests = requests.receiveAsFlow()
    private val mutableCatalogSnapshot = MutableStateFlow(CourseCatalogSnapshot())
    val catalogSnapshot: StateFlow<CourseCatalogSnapshot> = mutableCatalogSnapshot.asStateFlow()

    fun submitSearch(request: CourseSearchRequest) {
        requests.trySend(request)
    }

    fun updateCatalogSnapshot(snapshot: CourseCatalogSnapshot) {
        mutableCatalogSnapshot.value = snapshot
    }
}

data class CourseCatalogSnapshot(
    val semester: String = "115-1",
    val courses: List<CourseSummary> = emptyList(),
    val isLoading: Boolean = true,
    val failureMessage: String? = null,
)
