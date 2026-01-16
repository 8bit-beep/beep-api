package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.StudentScheduleDocs
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.domain.user.service.StudentScheduleService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/schedules")
class StudentScheduleController(
    private val studentScheduleService: StudentScheduleService
) : StudentScheduleDocs {
    @PostMapping
    override fun createSchedule(@RequestBody request: CreateStudentScheduleRequest) {
        studentScheduleService.create(request)
    }

    @GetMapping("/me")
    override fun getMySchedules(): List<StudentScheduleResponse> =
        studentScheduleService.getAll().map { StudentScheduleResponse.of(it) }

    @PatchMapping("/{scheduleId}")
    override fun updateSchedule(
        @PathVariable scheduleId: Long,
        @RequestBody request: UpdateStudentScheduleRequest
    ) {
        studentScheduleService.update(scheduleId, request)
    }

    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(@PathVariable scheduleId: Long) {
        studentScheduleService.delete(scheduleId)
    }
}
