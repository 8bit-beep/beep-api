package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.service.StudentAttendanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "출석", description = "출석 관련 API")
@Validated
@RestController
@RequestMapping("/attendances")
class StudentAttendanceController(
    private val studentAttendanceService: StudentAttendanceService
) {
    @Operation(summary = "출석하기", description = "학생이 출석을 합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun attend(@Valid @RequestBody request: CreateAttendanceRequest) {
        studentAttendanceService.attend(request)
    }

    @Operation(summary = "출석 취소", description = "학생이 출석을 취소합니다.")
    @PatchMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelAttendance() {
        studentAttendanceService.cancelAttendance()
    }
}
