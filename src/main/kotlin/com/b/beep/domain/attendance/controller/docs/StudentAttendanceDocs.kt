package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid

@Tag(name = "출석", description = "출석 관련 API")
interface StudentAttendanceDocs {
    @Operation(summary = "출석하기", description = "학생이 출석을 합니다.")
    fun attend(@Valid request: CreateAttendanceRequest)

    @Operation(summary = "출석 취소", description = "학생이 출석을 취소합니다.")
    fun cancelAttendance()
}
