package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.mpc.domain.model.CourseSearchRequest

class CourseSearchBridge {
    private val requests = Channel<CourseSearchRequest>(Channel.BUFFERED)
    internal val sendRequests = requests.receiveAsFlow()

    fun submitSearch(request: CourseSearchRequest) {
        requests.trySend(request)
    }
}
