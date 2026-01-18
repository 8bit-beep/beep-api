package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "출석 타입", description = "출석 타입 관리 API")
interface AttendanceTypeDocs {
    @Operation(summary = "출석 타입 생성", description = "새로운 출석 타입을 생성합니다.")
    fun createType(@Valid request: CreateAttendanceTypeRequest): AttendanceTypeResponse

    @Operation(summary = "출석 타입 목록 조회", description = "모든 출석 타입을 조회합니다.")
    fun getTypes(): List<AttendanceTypeResponse>

    @Operation(summary = "출석 타입 단건 조회", description = "ID로 출석 타입을 조회합니다.")
    fun getType(@Parameter(description = "출석 타입 ID") @Positive(message = "ID는 양수여야 합니다") typeId: Long): AttendanceTypeResponse

    @Operation(summary = "출석 타입 수정", description = "출석 타입을 수정합니다.")
    fun updateType(
        @Parameter(description = "출석 타입 ID") @Positive(message = "ID는 양수여야 합니다") typeId: Long,
        @Valid request: UpdateAttendanceTypeRequest
    ): AttendanceTypeResponse

    @Operation(summary = "출석 타입 삭제", description = "출석 타입을 삭제합니다.")
    fun deleteType(@Parameter(description = "출석 타입 ID") @Positive(message = "ID는 양수여야 합니다") typeId: Long)
}
