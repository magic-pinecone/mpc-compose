package org.mpc.domain.search

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.domain.model.CourseDay
import org.mpc.domain.model.CoursePeriod
import org.mpc.domain.model.CourseQuery
import org.mpc.domain.model.CourseSerialNo
import org.mpc.domain.model.CourseSummary
import org.mpc.domain.model.CourseTime
import org.mpc.domain.model.CourseType
import org.mpc.domain.model.PasswordCardType
import kotlin.test.assertEquals

private fun course(
    serialNo: String,
    title: String,
    teachers: List<String> = emptyList(),
    departmentName: String = "數學系",
    collegeName: String = "理學院",
    courseType: CourseType = CourseType.REQUIRED,
    classTimes: List<CourseTime> = emptyList(),
) = CourseSummary(
    serialNo = CourseSerialNo(serialNo),
    classNo = "01",
    title = title,
    credit = 3.0,
    passwordCard = PasswordCardType.NONE,
    teachers = teachers,
    classTimes = classTimes,
    limitCnt = 50,
    admitCnt = 0,
    waitCnt = 0,
    collegeName = collegeName,
    departmentName = departmentName,
    courseType = courseType,
    detailUrl = "",
)

private fun search(
    courses: List<CourseSummary>,
    query: CourseQuery,
) = searchCourses(courses, query).map { it.serialNo.value }

private fun query(
    keyword: String = "",
    teacher: String? = null,
    department: String? = null,
    days: Set<CourseDay> = emptySet(),
    periods: Set<CoursePeriod> = emptySet(),
    courseType: CourseType? = null,
) = CourseQuery("115-1", keyword, teacher, department, days, periods, courseType)

val courseSearchTests by testSuite {
    test("serial number hit outranks title hit") {
        val courses = listOf(
            course("10001", "12345 專題"),
            course("12345", "微積分"),
        )
        assertEquals(listOf("12345", "10001"), search(courses, query("12345")))
    }

    test("title prefix outranks title contains") {
        val courses = listOf(
            course("10001", "應用微積分"),
            course("10002", "微積分"),
            course("10003", "微積分二"),
        )
        assertEquals(listOf("10002", "10003", "10001"), search(courses, query("微積分")))
    }

    test("every keyword token must match some field") {
        val courses = listOf(
            course("10001", "微積分", teachers = listOf("李大明")),
            course("10002", "微積分", teachers = listOf("黃小華")),
            course("10003", "線性代數", teachers = listOf("黃小華")),
        )
        assertEquals(listOf("10002"), search(courses, query("微積分 黃")))
    }

    test("full-width and traditional variant queries match") {
        val courses = listOf(
            course("10001", "台灣史"),
            course("10002", "Calculus"),
        )
        assertEquals(listOf("10001"), search(courses, query("臺灣")))
        assertEquals(listOf("10002"), search(courses, query("ＣＡＬＣＵＬＵＳ")))
        assertEquals(listOf("10002"), search(courses, query("　calculus　")))
    }

    test("teacher and department filters narrow the catalog") {
        val courses = listOf(
            course("10001", "A", teachers = listOf("李大明"), departmentName = "數學系"),
            course("10002", "B", teachers = listOf("黃小華"), departmentName = "物理系", collegeName = "理學院"),
            course("10003", "C", teachers = listOf("黃小華"), departmentName = "中文系", collegeName = "文學院"),
        )
        assertEquals(listOf("10002", "10003"), search(courses, query(teacher = "黃")))
        assertEquals(listOf("10003"), search(courses, query(department = "文學院")))
        assertEquals(listOf("10002"), search(courses, query(teacher = "黃", department = "物理")))
    }

    test("day and period filters require one class time to satisfy both") {
        val monday1 = CourseTime(CourseDay.MONDAY, CoursePeriod.ONE)
        val tuesday1 = CourseTime(CourseDay.TUESDAY, CoursePeriod.ONE)
        val tuesday2 = CourseTime(CourseDay.TUESDAY, CoursePeriod.TWO)
        val courses = listOf(
            course("10001", "A", classTimes = listOf(monday1, tuesday2)),
            course("10002", "B", classTimes = listOf(tuesday1)),
        )
        assertEquals(listOf("10001"), search(courses, query(days = setOf(CourseDay.MONDAY))))
        assertEquals(listOf("10001"), search(courses, query(periods = setOf(CoursePeriod.TWO))))
        assertEquals(
            listOf("10001"),
            search(courses, query(days = setOf(CourseDay.TUESDAY), periods = setOf(CoursePeriod.TWO))),
        )
        assertEquals(
            emptyList(),
            search(courses, query(days = setOf(CourseDay.MONDAY), periods = setOf(CoursePeriod.TWO))),
        )
    }

    test("course type filter keeps matching type only") {
        val courses = listOf(
            course("10001", "A", courseType = CourseType.REQUIRED),
            course("10002", "B", courseType = CourseType.ELECTIVE),
        )
        assertEquals(listOf("10002"), search(courses, query(courseType = CourseType.ELECTIVE)))
    }

    test("blank query preserves catalog order") {
        val courses = listOf(course("10003", "C"), course("10001", "A"), course("10002", "B"))
        assertEquals(listOf("10003", "10001", "10002"), search(courses, query("   ")))
    }

    test("keyword matches teacher and department when title does not") {
        val courses = listOf(
            course("10001", "A", teachers = listOf("黃小華")),
            course("10002", "B", departmentName = "黃金系"),
            course("10003", "C"),
        )
        assertEquals(listOf("10001", "10002"), search(courses, query("黃")))
    }

    test("equal scores keep catalog order") {
        val courses = listOf(
            course("10003", "微積分甲"),
            course("10001", "微積分乙"),
            course("10002", "微積分丙"),
        )
        assertEquals(listOf("10003", "10001", "10002"), search(courses, query("微積分")))
    }
}
