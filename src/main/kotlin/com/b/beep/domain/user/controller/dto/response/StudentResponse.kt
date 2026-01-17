package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.entity.StudentInfoEntity

data class StudentResponse(
    val id: Long,
    val username: String,
    val email: String,
    val grade: Int,
    val classNumber: Int,
    val num: Int
) {
    companion object {
        fun of(studentInfo: StudentInfoEntity): StudentResponse {
            return StudentResponse(
                id = studentInfo.user.id!!,
                username = studentInfo.user.username,
                email = studentInfo.user.email,
                grade = studentInfo.grade,
                classNumber = studentInfo.classNumber,
                num = studentInfo.num
            )
        }
    }
}
