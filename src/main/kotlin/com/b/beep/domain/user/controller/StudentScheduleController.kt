package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.dto.request.CreateMyScheduleRequest
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.domain.user.service.StudentScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "학생 스케줄", description = "학생 교시별 실 등록 API")
@Validated
@RestController
@RequestMapping("/schedules")
class StudentScheduleController(
    private val studentScheduleService: StudentScheduleService
) {
    @Operation(summary = "스케줄 생성", description = "특정 유저의 스케줄을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSchedule(@Valid @RequestBody request: CreateStudentScheduleRequest) {
        studentScheduleService.createSchedule(request)
    }

    @Operation(summary = "유저별 스케줄 조회", description = "특정 유저의 스케줄 목록을 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSchedulesByUserId(
        @Parameter(description = "유저 ID") @RequestParam @Positive(message = "유저 ID는 양수여야 합니다") userId: Long
    ): List<StudentScheduleResponse> =
        studentScheduleService.getSchedulesByUserId(userId)

    @Operation(summary = "내 스케줄 생성", description = "로그인한 사용자의 스케줄을 생성합니다.")
    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMySchedule(@Valid @RequestBody request: CreateMyScheduleRequest) {
        studentScheduleService.createMySchedule(request)
    }

    @Operation(summary = "내 스케줄 조회", description = "로그인한 사용자의 스케줄 목록을 조회합니다.")
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    fun getMySchedules(): List<StudentScheduleResponse> =
        studentScheduleService.getMySchedules()

    @Operation(summary = "스케줄 수정", description = "스케줄 정보를 수정합니다.")
    @PatchMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long,
        @Valid @RequestBody request: UpdateStudentScheduleRequest
    ) {
        studentScheduleService.updateSchedule(scheduleId, request)
    }

    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다.")
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long
    ) {
        studentScheduleService.deleteSchedule(scheduleId)
    }
}
