package com.b.beep.domain.auth.infrastructure

data class StudentInfo(
    val grade: Int,
    val room: Int,
    val number: Int,
    val isGraduated: Boolean = false
)