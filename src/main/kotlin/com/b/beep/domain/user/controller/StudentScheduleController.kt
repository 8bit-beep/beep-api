package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.StudentScheduleDocs
import com.b.beep.domain.user.controller.dto.request.CreateMyScheduleRequest
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.domain.user.service.StudentScheduleService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/schedules")
class StudentScheduleController(
    private val studentScheduleService: StudentScheduleService
) : StudentScheduleDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createSchedule(@Valid @RequestBody request: CreateStudentScheduleRequest) {
        studentScheduleService.create(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getSchedulesByUserId(
        @RequestParam @Positive(message = "유저 ID는 양수여야 합니다") userId: Long
    ): List<StudentScheduleResponse> =
        studentScheduleService.getByUserId(userId).map { StudentScheduleResponse.of(it) }

    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createMySchedule(@Valid @RequestBody request: CreateMyScheduleRequest) {
        studentScheduleService.createMy(request)
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    override fun getMySchedules(): List<StudentScheduleResponse> =
        studentScheduleService.getMySchedules().map { StudentScheduleResponse.of(it) }

    @PatchMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long,
        @Valid @RequestBody request: UpdateStudentScheduleRequest
    ) {
        studentScheduleService.update(scheduleId, request)
    }

    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long
    ) {
        studentScheduleService.delete(scheduleId)
    }
}
