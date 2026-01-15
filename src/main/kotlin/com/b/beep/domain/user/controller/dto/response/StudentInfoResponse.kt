package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.enums.entity.StudentInfoEntity

data class StudentInfoResponse(
    val id: Long? = null,
    val grade: Int,
    val classNumber: Int,
    val num: Int,
    val cardId: String? = null,
) {
    companion object {
        fun of(studentInfo: StudentInfoEntity): StudentInfoResponse {
            return StudentInfoResponse(
                id = studentInfo.id,
                grade = studentInfo.grade,
                classNumber = studentInfo.classNumber,
                num = studentInfo.num,
                cardId = studentInfo.cardId,
            )
        }
    }
}
