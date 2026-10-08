package org.mpc.bridge

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.mpc.domain.model.CourseSummary

class CoursePlanBridge {
    private val requests = Channel<Unit>(Channel.BUFFERED)
    internal val saveRequests = requests.receiveAsFlow()
    private val courseToggleRequests = Channel<CourseSummary>(Channel.BUFFERED)
    internal val toggleRequests = courseToggleRequests.receiveAsFlow()
    private var latestSelectedCourses: List<CourseSummary> = emptyList()
    private var latestCanEditPlan = false
    val canEditPlan: Boolean get() = latestCanEditPlan
    private var selectedCoursesObserver: ((List<CourseSummary>) -> Unit)? = null
    private var courseDetailsObserver: ((String, CourseSummary) -> Unit)? = null

    fun requestToggleCourse(course: CourseSummary) {
        courseToggleRequests.trySend(course)
    }

    fun observeCourseDetailsRequest(observer: ((String, CourseSummary) -> Unit)?) {
        courseDetailsObserver = observer
    }

    fun requestCourseDetails(semester: String, course: CourseSummary) {
        courseDetailsObserver?.invoke(semester, course)
    }

    fun observeSelectedCourses(observer: ((List<CourseSummary>) -> Unit)?) {
        selectedCoursesObserver = observer
        observer?.invoke(latestSelectedCourses)
    }

    fun updateSelectedCourses(courses: List<CourseSummary>, canEditPlan: Boolean) {
        if (latestSelectedCourses == courses && latestCanEditPlan == canEditPlan) return

        latestSelectedCourses = courses
        latestCanEditPlan = canEditPlan
        selectedCoursesObserver?.invoke(courses)
    }

    fun requestSave() {
        requests.trySend(Unit)
    }
}
