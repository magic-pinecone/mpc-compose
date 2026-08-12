package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class CoursePlanBridge {
    private val requests = Channel<Unit>(Channel.BUFFERED)
    internal val saveRequests = requests.receiveAsFlow()

    fun requestSave() {
        requests.trySend(Unit)
    }
}
