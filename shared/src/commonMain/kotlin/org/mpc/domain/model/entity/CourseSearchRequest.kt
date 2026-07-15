package org.mpc.domain.model.entity

data class CourseSearchRequest(
    val semester: String,
    val query: String
)