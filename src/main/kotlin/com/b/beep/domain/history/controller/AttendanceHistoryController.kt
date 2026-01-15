package com.b.beep.domain.history.controller

import com.b.beep.domain.history.controller.docs.AttendanceHistoryDocs
import com.b.beep.domain.history.controller.dto.response.ClassAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.RoomAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.StudentAttendanceRecord
import com.b.beep.domain.history.service.AttendanceHistoryService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/history")
class AttendanceHistoryController(
    private val attendanceHistoryService: AttendanceHistoryService
) : AttendanceHistoryDocs {
    @GetMapping("/by-class")
    @ResponseStatus(HttpStatus.OK)
    override fun getByClass(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<ClassAttendanceHistoryResponse> {
        return attendanceHistoryService.getByClass(date)
    }

    @GetMapping("/by-room")
    @ResponseStatus(HttpStatus.OK)
    override fun getByRoom(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<RoomAttendanceHistoryResponse> {
        return attendanceHistoryService.getByRoom(date)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAll(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<StudentAttendanceRecord> {
        return attendanceHistoryService.getAll(date)
    }
}
