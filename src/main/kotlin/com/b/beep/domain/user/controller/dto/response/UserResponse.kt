package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.user.domain.enums.UserRole

data class UserResponse(
    val id: Long? = null,
    val email: String,
    val username: String,
    val role: UserRole,
    val profileImage: String? = null,
    val studentInfo: StudentInfoResponse? = null,
    val currentStatus: AttendanceTypeResponse? = null
)
