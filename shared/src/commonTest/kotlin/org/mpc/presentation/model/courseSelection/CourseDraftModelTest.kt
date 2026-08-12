package org.mpc.presentation.model.courseSelection

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.domain.model.CoursePlan
import org.mpc.domain.model.CourseSummary

private fun emptyCoursePlan(): CoursePlan = CoursePlan(
    semester = "115-1",
    selectedCourses = mapOf()
)

private val freshmanPE = CourseSummary(
        serialNo = "01001",
        classNo = "PE1011-A",
    大一體育",
        "credit": 0.0,
        "password_card": "OPTIONAL",
        "teachers": [
        "沈淑貞"
        ],
        "class_times": [
        "1-3",
        "1-4"
        ],
        "limit_cnt": 0,
        "admit_cnt": 0,
        "wait_cnt": 0,
        "college_name": "中心、處室",
        "department_name": "體育室",
        "course_type": "REQUIRED",
        "detail_url": "https://cis.ncu.edu.tw/Course/main/support/courseDetail.html?crs=01001"
)


val basicCoursePlanTest by testSuite {
    val coursePlan = testFixture{
        emptyCoursePlan()
    } asContextForAll {
        test("Course is added to the plan") {

        }
    }
}