package org.mpc.presentation.state

import org.mpc.domain.model.entity.CourseDay
import org.mpc.domain.model.entity.CoursePeriod
import org.mpc.domain.model.entity.CourseSerialNo
import org.mpc.domain.model.entity.CourseSummary
import org.mpc.domain.model.entity.CourseTime
import org.mpc.domain.model.entity.CourseType
import org.mpc.domain.model.entity.PasswordCardType
import org.mpc.domain.model.snapshot.CoursePlanSnapshot
import org.mpc.domain.model.state.CoursePlanState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class CoursePlanDraftStoreTest {
    @Test
    fun toggleCourseUpdatesTheSharedStateFlow() {
        val store = CoursePlanDraftStore()
        val searchState = store.state
        val timetableState = store.state
        val course = courseSummary()

        store.acceptLoadedSnapshot(emptySnapshot())
        store.toggleCourse(course)

        assertSame(searchState, timetableState)
        val state = assertIs<CoursePlanState.Success>(timetableState.value)
        assertEquals(course, state.snapshot.selected[course.serialNo])
    }

    @Test
    fun toggleCourseRemovesAnAlreadySelectedCourse() {
        val store = CoursePlanDraftStore()
        val course = courseSummary()

        store.acceptLoadedSnapshot(
            emptySnapshot().copy(selected = mapOf(course.serialNo to course))
        )
        store.toggleCourse(course)

        val state = assertIs<CoursePlanState.Success>(store.state.value)
        assertEquals(emptyMap(), state.snapshot.selected)
    }

    @Test
    fun loadingTheSameSemesterDoesNotReplaceUnsavedChanges() {
        val store = CoursePlanDraftStore()
        val course = courseSummary()

        store.acceptLoadedSnapshot(emptySnapshot())
        store.toggleCourse(course)
        store.acceptLoadedSnapshot(emptySnapshot())

        val state = assertIs<CoursePlanState.Success>(store.state.value)
        assertEquals(course, state.snapshot.selected[course.serialNo])
    }

    @Test
    fun aLateLoadFailureDoesNotReplaceAnExistingDraft() {
        val store = CoursePlanDraftStore()

        store.acceptLoadedSnapshot(emptySnapshot())
        store.acceptLoadFailure(IllegalStateException("late failure"))

        assertIs<CoursePlanState.Success>(store.state.value)
    }
}

private fun emptySnapshot() = CoursePlanSnapshot(
    semester = "115-1",
    selected = emptyMap(),
)

private fun courseSummary() = CourseSummary(
    serialNo = CourseSerialNo("12345"),
    classNo = "CS101",
    title = "Functional Programming",
    credit = 3.0,
    passwordCard = PasswordCardType.NONE,
    teachers = listOf("Teacher"),
    classTimes = listOf(
        CourseTime(CourseDay.MONDAY, CoursePeriod.ONE)
    ),
    limitCnt = 50,
    admitCnt = 0,
    waitCnt = 0,
    collegeName = "College",
    departmentName = "Department",
    courseType = CourseType.ELECTIVE,
    detailUrl = "https://example.com/course/12345",
)
