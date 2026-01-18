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
        fun of(entity: StudentInfoEntity): StudentResponse {
            return StudentResponse(
                id = entity.user.id!!,
                username = entity.user.username,
                email = entity.user.email,
                grade = entity.grade,
                classNumber = entity.classNumber,
                num = entity.num
            )
        }
    }
}
