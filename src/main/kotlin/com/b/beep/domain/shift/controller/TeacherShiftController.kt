package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.TeacherShiftDocs
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.service.TeacherShiftService
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/shifts")
class TeacherShiftController(
    private val teacherShiftService: TeacherShiftService,
) : TeacherShiftDocs {
    @GetMapping
    override fun getShifts(): List<ShiftResponse> {
        return teacherShiftService.getAll()
    }

    @PatchMapping("/{shiftId}/status")
    override fun updateShiftStatus(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long,
        @RequestParam status: ShiftStatus
    ) {
        teacherShiftService.updateStatus(shiftId, status)
    }
}
