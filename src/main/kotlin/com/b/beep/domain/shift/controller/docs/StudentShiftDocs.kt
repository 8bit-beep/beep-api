package com.b.beep.domain.shift.controller.docs

import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "실이동")
interface StudentShiftDocs {
    @Operation(summary = "실이동 신청")
    fun createShift(@Valid request: CreateShiftRequest)

    @Operation(summary = "나의 실이동 신청 목록 조회")
    fun getMyShifts(): List<ShiftResponse>

    @Operation(summary = "실이동 수정")
    fun updateShift(@Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long, @Valid request: UpdateShiftRequest)

    @Operation(summary = "실이동 삭제")
    fun deleteShift(@Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long)
}
