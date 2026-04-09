package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.entity.StudentInfoEntity

data class StudentResponse(
    val id: Long,
    val username: String,
    val name: String,
    val profileImage: String?,
    val studentInfo: StudentInfoResponse
) {
    companion object {
        fun of(entity: StudentInfoEntity): StudentResponse {
            return StudentResponse(
                id = entity.user.id!!,
                username = entity.user.username,
                name = entity.user.name,
                profileImage = entity.user.profileImage,
                studentInfo = StudentInfoResponse.of(entity)
            )
        }
    }
}
