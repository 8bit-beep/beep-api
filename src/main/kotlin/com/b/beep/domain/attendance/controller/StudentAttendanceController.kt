package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.controller.dto.request.ScanHelpQrRequest
import com.b.beep.domain.attendance.controller.dto.response.HelpQrResponse
import com.b.beep.domain.attendance.service.AttendanceHelpService
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
    private val studentAttendanceService: StudentAttendanceService,
    private val attendanceHelpService: AttendanceHelpService
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

    @Operation(summary = "출석 도움 QR 생성", description = "다른 학생의 출석을 도와주기 위한 QR 토큰을 생성합니다.")
    @PostMapping("/help/qr")
    @ResponseStatus(HttpStatus.CREATED)
    fun generateHelpQr(): HelpQrResponse {
        return attendanceHelpService.generateHelpQr()
    }

    @Operation(summary = "출석 도움 QR 스캔", description = "QR을 스캔하여 출석합니다.")
    @PostMapping("/help/scan")
    @ResponseStatus(HttpStatus.CREATED)
    fun scanHelpQr(@Valid @RequestBody request: ScanHelpQrRequest) {
        attendanceHelpService.scanHelpQr(request)
    }
}
