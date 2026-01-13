package com.b.beep.domain.history.controller.docs

import com.b.beep.domain.history.controller.dto.response.ClassAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.RoomAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.StudentAttendanceRecord
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Tag(name = "출석 기록", description = "출석 기록 API")
interface AttendanceHistoryDocs {
    @Operation(summary = "반별 출석 기록 조회")
    fun getByClass(@RequestParam date: LocalDate): List<ClassAttendanceHistoryResponse>

    @Operation(summary = "실별 출석 기록 조회")
    fun getByRoom(@RequestParam date: LocalDate): List<RoomAttendanceHistoryResponse>

    @Operation(summary = "전체 출석 기록 조회")
    fun getAll(@RequestParam date: LocalDate): List<StudentAttendanceRecord>
}
