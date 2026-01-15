package com.b.beep.domain.attendance.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import java.time.LocalDate

data class UpdateStatusRequest(
    val grade: Int,
    val classNumber: Int,
    val num: Int,
    val status: AttendanceType,
    val date: LocalDate? = null,
    val period: Int? = null
)
