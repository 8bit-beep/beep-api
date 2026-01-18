package com.b.beep.domain.shift.controller.docs

import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive

@Tag(name = "실이동 관리자")
interface TeacherShiftDocs {
    @Operation(summary = "실이동 목록 조회")
    fun getShifts(): List<ShiftResponse>

    @Operation(summary = "실이동 상태 변경")
    fun updateShiftStatus(@Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long, status: ShiftStatus)
}