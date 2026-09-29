package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class CoursePlanBridge {
    private val requests = Channel<Unit>(Channel.BUFFERED)
    internal val saveRequests = requests.receiveAsFlow()
    private var latestSelectedCourseCount = 0
    private var selectedCourseCountObserver: ((Int) -> Unit)? = null

    fun observeSelectedCourseCount(observer: ((Int) -> Unit)?) {
        selectedCourseCountObserver = observer
        observer?.invoke(latestSelectedCourseCount)
    }

    fun updateSelectedCourseCount(count: Int) {
        if (latestSelectedCourseCount == count) return

        latestSelectedCourseCount = count
        selectedCourseCountObserver?.invoke(count)
    }

    fun requestSave() {
        requests.trySend(Unit)
    }
}
