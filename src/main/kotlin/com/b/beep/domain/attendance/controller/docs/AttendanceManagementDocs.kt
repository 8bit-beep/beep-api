package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.request.PreAttendRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "출석 관리", description = "출석 상태 관리 API")
interface AttendanceManagementDocs {
    @Operation(summary = "사전 출석 설정", description = "특정 학생의 출석 상태를 미리 설정합니다.")
    fun preAttend(request: PreAttendRequest)

    @Operation(summary = "출석 상태 변경", description = "현재 교시의 학생 출석 상태를 변경합니다.")
    fun updateStatus(request: UpdateStatusRequest)
}
