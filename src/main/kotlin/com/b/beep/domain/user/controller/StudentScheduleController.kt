package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.StudentScheduleDocs
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.domain.user.service.StudentScheduleService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/schedules")
class StudentScheduleController(
    private val studentScheduleService: StudentScheduleService
) : StudentScheduleDocs {
    @PostMapping
    override fun createSchedule(@Valid @RequestBody request: CreateStudentScheduleRequest) {
        studentScheduleService.create(request)
    }

    @GetMapping("/me")
    override fun getMySchedules(): List<StudentScheduleResponse> =
        studentScheduleService.getAll().map { StudentScheduleResponse.of(it) }

    @PatchMapping("/{scheduleId}")
    override fun updateSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long,
        @Valid @RequestBody request: UpdateStudentScheduleRequest
    ) {
        studentScheduleService.update(scheduleId, request)
    }

    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @PathVariable @Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long
    ) {
        studentScheduleService.delete(scheduleId)
    }
}
