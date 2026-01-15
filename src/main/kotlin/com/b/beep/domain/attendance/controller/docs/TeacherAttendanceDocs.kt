package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.request.PreAttendRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "출석 관리", description = "출석 관리 API")
interface TeacherAttendanceDocs {
    @Operation(summary = "출석 상태 변경", description = "현재 교시의 학생 출석 상태를 변경합니다.")
    fun updateStatus(request: UpdateStatusRequest)

    @Operation(summary = "학생 조회", description = "조건에 맞는 학생 목록을 조회합니다.")
    fun getAll(
        @Parameter(description = "실 ID") roomId: Long?,
        @Parameter(description = "출석 타입") type: AttendanceType?,
        @Parameter(description = "출석 상태 필터") status: AttendanceType?,
        @Parameter(description = "학년") grade: Int?,
        @Parameter(description = "반") classNumber: Int?
    ): List<AttendanceStudentResponse>
}
