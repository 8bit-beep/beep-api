package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.service.TeacherShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "실이동 관리자")
@Validated
@RestController
@RequestMapping("/shifts")
class TeacherShiftController(
    private val teacherShiftService: TeacherShiftService,
) {
    @Operation(summary = "실이동 목록 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getShifts(): List<ShiftResponse> {
        return teacherShiftService.getShifts()
    }

    @Operation(summary = "실이동 상태 변경")
    @PatchMapping("/{shiftId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateShiftStatus(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long,
        @RequestParam status: ShiftStatus
    ) {
        teacherShiftService.updateStatus(shiftId, status)
    }
}
