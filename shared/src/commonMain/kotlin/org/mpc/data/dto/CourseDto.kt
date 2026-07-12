package org.mpc.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
{
      "serial_no": "01001",
      "class_no": "PE1011-A",
      "title": "大一體育",
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
    },
 */
@Serializable
data class CourseDto(
    val serialNo: String,
    val classNo: String,
    val title: String,
    val credit: Double,
    val passwordCard: PasswordCardTypeDto,
    val teachers: Array<String>,
    val classTimes: Array<String>,
    val limitCnt: Int,
    val adminCnt: Int,
    val waitCnt: Int,
    val collegeName: String,
    val departmentName: String,
    val courseType: CourseTypeDto,
    val detailUrl: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CourseDto

        if (credit != other.credit) return false
        if (limitCnt != other.limitCnt) return false
        if (adminCnt != other.adminCnt) return false
        if (waitCnt != other.waitCnt) return false
        if (serialNo != other.serialNo) return false
        if (classNo != other.classNo) return false
        if (title != other.title) return false
        if (passwordCard != other.passwordCard) return false
        if (!teachers.contentEquals(other.teachers)) return false
        if (!classTimes.contentEquals(other.classTimes)) return false
        if (collegeName != other.collegeName) return false
        if (departmentName != other.departmentName) return false
        if (courseType != other.courseType) return false
        if (detailUrl != other.detailUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credit.hashCode()
        result = 31 * result + limitCnt
        result = 31 * result + adminCnt
        result = 31 * result + waitCnt
        result = 31 * result + serialNo.hashCode()
        result = 31 * result + classNo.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + passwordCard.hashCode()
        result = 31 * result + teachers.contentHashCode()
        result = 31 * result + classTimes.contentHashCode()
        result = 31 * result + collegeName.hashCode()
        result = 31 * result + departmentName.hashCode()
        result = 31 * result + courseType.hashCode()
        result = 31 * result + detailUrl.hashCode()
        return result
    }
}

@Serializable
enum class PasswordCardTypeDto {
    @SerialName("REQUIRED")
    REQUIRED,
    @SerialName("OPTIONAL")
    OPTIONAL,
    @SerialName("UNKNOWN")
    UNKNOWN,
}

@Serializable
enum class CourseTypeDto {
    @SerialName("REQUIRED")
    REQUIRED,
    @SerialName("OPTIONAL")
    OPTIONAL,
    @SerialName("UNKNOWN")
    UNKNOWN,
}
