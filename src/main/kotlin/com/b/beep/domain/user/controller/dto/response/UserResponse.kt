package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.domain.enums.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole

data class UserResponse(
    val id: Long? = null,
    val email: String,
    val username: String,
    val role: UserRole,
    val profileImage: String? = null,
    var studentInfo: StudentInfoResponse? = null,
    val currentStatus: AttendanceType? = null
) {
    companion object {
        fun of(
            user: UserEntity,
            studentInfo: StudentInfoEntity? = null,
            currentStatus: AttendanceType? = null
        ): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                role = user.role,
                profileImage = user.profileImage,
                studentInfo = studentInfo?.let { StudentInfoResponse.of(it) },
                currentStatus = currentStatus
            )
        }
    }
}
