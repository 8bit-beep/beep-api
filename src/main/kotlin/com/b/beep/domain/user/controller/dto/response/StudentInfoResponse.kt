package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.entity.StudentInfoEntity

data class StudentInfoResponse(
    val id: Long? = null,
    val grade: Int,
    val classNumber: Int,
    val num: Int,
    val cardId: String? = null,
) {
    companion object {
        fun of(entity: StudentInfoEntity): StudentInfoResponse {
            return StudentInfoResponse(
                id = entity.id,
                grade = entity.grade,
                classNumber = entity.classNumber,
                num = entity.num,
                cardId = entity.cardId,
            )
        }
    }
}
