package com.b.beep.domain.attendance.controller.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Tag(name = "출석 기록", description = "출석 기록 API")
interface AttendanceHistoryDocs {
    @Operation(summary = "출석 엑셀 파일 목록 조회")
    fun listFiles(): List<String>

    @Operation(summary = "출석 엑셀 다운로드 URL 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "다운로드 URL 반환"),
        ApiResponse(responseCode = "404", description = "해당 날짜의 파일이 존재하지 않음")
    )
    fun downloadByDate(@RequestParam date: LocalDate): ResponseEntity<Map<String, String>>
}
