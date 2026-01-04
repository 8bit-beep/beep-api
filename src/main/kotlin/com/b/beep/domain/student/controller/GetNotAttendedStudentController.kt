package com.b.beep.domain.student.controller

import com.b.beep.domain.student.controller.docs.GetNotAttendedStudentDocs
import com.b.beep.domain.student.controller.dto.response.StudentResponse
import com.b.beep.domain.student.service.GetNotAttendedStudentService
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students/not-attend")
class GetNotAttendedStudentController(
    private val getNotAttendedStudentService: GetNotAttendedStudentService
) : GetNotAttendedStudentDocs {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAll(
        @RequestParam type: AttendanceType
    ): List<StudentResponse> {
        return getNotAttendedStudentService.getAll(type)
    }

    @GetMapping("/room")
    @ResponseStatus(HttpStatus.OK)
    override fun getAllByRoomAndType(
        @RequestParam roomId: Long,
        @RequestParam type: AttendanceType
    ): List<StudentResponse> {
        return getNotAttendedStudentService.getAllByRoomAndType(roomId, type)
    }

    @GetMapping("/class")
    @ResponseStatus(HttpStatus.OK)
    override fun getAllByGradeAndCls(
        @RequestParam grade: Int,
        @RequestParam cls: Int,
    ): List<StudentResponse> {
        return getNotAttendedStudentService.getAllByGradeAndCls(grade, cls)
    }
}
