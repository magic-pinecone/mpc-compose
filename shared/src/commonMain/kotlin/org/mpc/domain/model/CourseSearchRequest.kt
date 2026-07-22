package org.mpc.domain.model

data class CourseSearchRequest(
    val semester: String,
    val query: String,
)
