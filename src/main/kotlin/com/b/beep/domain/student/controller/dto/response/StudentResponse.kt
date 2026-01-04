package com.b.beep.domain.student.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.entity.StudentInfoEntity
import com.b.beep.domain.room.fixedroom.entity.FixedRoomEntity
import com.b.beep.domain.attendance.entity.AttendanceEntity
import com.b.beep.domain.room.fixedroom.controller.dto.response.FixedRoomResponse

data class StudentResponse(
    val username: String,
    val studentId: String,
    val fixedRooms: List<FixedRoomResponse>?,
    val statuses: List<StatusResponse>
) {
    companion object {
        fun of(
            user: UserEntity,
            studentInfo: StudentInfoEntity,
            fixedRooms: List<FixedRoomEntity>?,
            attendances: List<AttendanceEntity>
        ): StudentResponse {
            return StudentResponse(
                username = user.username,
                studentId = String.format("%d%d%02d", studentInfo.grade, studentInfo.cls, studentInfo.num),
                fixedRooms = fixedRooms?.map { FixedRoomResponse.from(it) },
                statuses = attendances.map { StatusResponse(it.period, it.type) }
            )
        }
    }
}

data class StatusResponse(
    val period: Int,
    val status: AttendanceType
)
