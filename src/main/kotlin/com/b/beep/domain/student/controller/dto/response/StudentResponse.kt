package com.b.beep.domain.student.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.entity.StudentInfoEntity
import com.b.beep.domain.attendance.entity.AttendanceEntity

data class StudentResponse(
    val username: String,
    val studentId: String,
    val statuses: List<StatusResponse>
) {
    companion object {
        fun of(
            user: UserEntity,
            studentInfo: StudentInfoEntity,
            attendances: List<AttendanceEntity>
        ): StudentResponse {
            return StudentResponse(
                username = user.username,
                studentId = String.format("%d%d%02d", studentInfo.grade, studentInfo.cls, studentInfo.num),
                statuses = attendances.map { StatusResponse(it.period, it.type) }
            )
        }
    }
}

data class StatusResponse(
    val period: Int,
    val status: AttendanceType
)
