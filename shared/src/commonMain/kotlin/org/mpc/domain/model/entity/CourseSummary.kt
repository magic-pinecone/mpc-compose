package org.mpc.domain.model.entity

data class CourseSummary (
    val serialNo: String,
    val classNo: String,
    val title: String,
    val credit: Double,
    val passwordCard: PasswordCardType,
    val teachers: List<String>,
    val classTimes: List<String>,
    val limitCnt: Int,
    val adminCnt: Int,
    val waitCnt: Int,
    val collegeName: String,
    val departmentName: String,
    val courseType: CourseType,
    val detailUrl: String
)

enum class PasswordCardType(val description: String) {
    ALL("全部"),
    OPTIONAL("部分"),
    NONE("無")
}

enum class CourseType(val description: String) {
    REQUIRED("必修"),
    ELECTIVE("選修"),
    UNKNOWN("未知")
}
