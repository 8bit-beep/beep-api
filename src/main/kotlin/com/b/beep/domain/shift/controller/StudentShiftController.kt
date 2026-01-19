package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.service.StudentShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "실이동")
@Validated
@RestController
@RequestMapping("/shifts")
class StudentShiftController(
    private val studentShiftService: StudentShiftService,
) {
    @Operation(summary = "실이동 신청")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createShift(@Valid @RequestBody request: CreateShiftRequest) {
        studentShiftService.createShift(request)
    }

    @Operation(summary = "나의 실이동 신청 목록 조회")
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    fun getMyShifts(): List<ShiftResponse> {
        return studentShiftService.getMyShifts()
    }

    @Operation(summary = "실이동 수정")
    @PatchMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateShift(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long,
        @Valid @RequestBody request: UpdateShiftRequest
    ) {
        studentShiftService.updateShift(shiftId, request)
    }

    @Operation(summary = "실이동 삭제")
    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteShift(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long
    ) {
        studentShiftService.deleteShift(shiftId)
    }
}
