package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Tag(name = "출석 관리", description = "출석 관리 API")
interface TeacherAttendanceDocs {
    @Operation(summary = "출석 상태 변경", description = "학생 출석 상태를 변경합니다. date와 checkpointId 미입력시 오늘, 현재 체크포인트 기반으로 수정합니다. statudId란에는 type을 입력해주세요.")
    fun updateStatus(request: UpdateStatusRequest)

    @Operation(summary = "학생 조회", description = "조건에 맞는 학생 목록을 조회합니다. roomId 입력 시 해당 실 스케줄 학생만 조회됩니다.")
    fun getAll(
        @Parameter(description = "실 ID (입력 시 해당 실 스케줄 학생만 조회)") roomId: Long?,
        @Parameter(description = "출석 상태 ID(type ID) 필터") statusId: Long?,
        @Parameter(description = "학년") grade: Int?,
        @Parameter(description = "반") classNumber: Int?,
        pageable: Pageable
    ): Page<AttendanceStudentResponse>
}
