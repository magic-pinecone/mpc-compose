package org.mpc.domain.model

data class CourseItem (
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
)

enum class PasswordCardType(description: String) {
    REQUIRED("全部"),
    OPTIONAL("部分"),
    UNKNOWN("未知")
}

enum class CourseType(description: String) {
    REQUIRED("必修"),
    OPTIONAL("選修"),
    UNKNOWN("未知")
}