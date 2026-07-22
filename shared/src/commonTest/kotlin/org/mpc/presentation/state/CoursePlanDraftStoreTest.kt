package org.mpc.presentation.state

import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CoursePlan
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.PasswordCardType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class CoursePlanDraftStoreTest {
    @Test
    fun toggleCourseUpdatesTheSharedStateFlow() {
        val store = CoursePlanDraftStore()
        val searchUiState = store.uiState
        val timetableUiState = store.uiState
        val course = courseSummary()

        store.acceptLoadedPlan(emptyPlan())
        store.toggleCourse(course)

        assertSame(searchUiState, timetableUiState)
        val uiState = assertIs<CoursePlanUiState.Success>(timetableUiState.value)
        assertEquals(course, uiState.plan.selectedCourses[course.serialNo])
    }

    @Test
    fun toggleCourseRemovesAnAlreadySelectedCourse() {
        val store = CoursePlanDraftStore()
        val course = courseSummary()

        store.acceptLoadedPlan(
            emptyPlan().copy(selectedCourses = mapOf(course.serialNo to course)),
        )
        store.toggleCourse(course)

        val uiState = assertIs<CoursePlanUiState.Success>(store.uiState.value)
        assertEquals(emptyMap(), uiState.plan.selectedCourses)
    }

    @Test
    fun loadingTheSameSemesterDoesNotReplaceUnsavedChanges() {
        val store = CoursePlanDraftStore()
        val course = courseSummary()

        store.acceptLoadedPlan(emptyPlan())
        store.toggleCourse(course)
        store.acceptLoadedPlan(emptyPlan())

        val uiState = assertIs<CoursePlanUiState.Success>(store.uiState.value)
        assertEquals(course, uiState.plan.selectedCourses[course.serialNo])
    }

    @Test
    fun aLateLoadFailureDoesNotReplaceAnExistingDraft() {
        val store = CoursePlanDraftStore()

        store.acceptLoadedPlan(emptyPlan())
        store.acceptLoadFailure(IllegalStateException("late failure"))

        assertIs<CoursePlanUiState.Success>(store.uiState.value)
    }
}

private fun emptyPlan() =
    CoursePlan(
        semester = "115-1",
        selectedCourses = emptyMap(),
    )

private fun courseSummary() =
    CourseSummary(
        serialNo = CourseSerialNo("12345"),
        classNo = "CS101",
        title = "Functional Programming",
        credit = 3.0,
        passwordCard = PasswordCardType.NONE,
        teachers = listOf("Teacher"),
        classTimes =
            listOf(
                CourseTime(CourseDay.MONDAY, CoursePeriod.ONE),
            ),
        limitCnt = 50,
        admitCnt = 0,
        waitCnt = 0,
        collegeName = "College",
        departmentName = "Department",
        courseType = CourseType.ELECTIVE,
        detailUrl = "https://example.com/course/12345",
    )
