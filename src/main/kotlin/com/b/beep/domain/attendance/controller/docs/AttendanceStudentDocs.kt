package com.b.beep.domain.attendance.controller.docs

import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "출석 학생 조회", description = "출석 상태 기반 학생 조회 API")
interface AttendanceStudentDocs {
    @Operation(summary = "학생 조회", description = "조건에 맞는 학생 목록을 조회합니다.")
    fun getAll(
        @Parameter(description = "실") room: Room?,
        @Parameter(description = "출석 타입") type: AttendanceType?,
        @Parameter(description = "출석 상태 필터") status: AttendanceType?,
        @Parameter(description = "학년") grade: Int?,
        @Parameter(description = "반") cls: Int?
    ): List<AttendanceStudentResponse>
}
