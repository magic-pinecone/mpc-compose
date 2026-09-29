package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.mpc.domain.model.CourseSummary

class CoursePlanBridge {
    private val requests = Channel<Unit>(Channel.BUFFERED)
    internal val saveRequests = requests.receiveAsFlow()
    private var latestSelectedCourses: List<CourseSummary> = emptyList()
    private var selectedCoursesObserver: ((List<CourseSummary>) -> Unit)? = null

    fun observeSelectedCourses(observer: ((List<CourseSummary>) -> Unit)?) {
        selectedCoursesObserver = observer
        observer?.invoke(latestSelectedCourses)
    }

    fun updateSelectedCourses(courses: List<CourseSummary>) {
        if (latestSelectedCourses == courses) return

        latestSelectedCourses = courses
        selectedCoursesObserver?.invoke(courses)
    }

    fun requestSave() {
        requests.trySend(Unit)
    }
}
