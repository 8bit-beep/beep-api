package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.service.StudentAttendanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "출석", description = "출석 관련 API")
@RestController
@RequestMapping("/attendances/beacon")
class BeaconAttendanceController(
    private val studentAttendanceService: StudentAttendanceService
) {
    @Operation(summary = "비콘으로 출석하기", description = "앱에서 선택한 실의 비콘을 찾은 뒤 학생이 출석합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun attend(@Valid @RequestBody request: CreateAttendanceRequest) {
        studentAttendanceService.attend(request)
    }
}
