package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.service.AttendanceHistoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "출석 기록", description = "출석 기록 API")
@RestController
@RequestMapping("/attendances/histories")
class AttendanceHistoryController(
    private val attendanceHistoryService: AttendanceHistoryService,
) {
    @Operation(summary = "출석 엑셀 파일 목록 조회")
    @GetMapping
    fun listFiles(): List<String> {
        return attendanceHistoryService.listFiles()
    }

    @Operation(summary = "출석 엑셀 다운로드 URL 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "다운로드 URL 반환"),
        ApiResponse(responseCode = "400", description = "미래 날짜 요청")
    )
    @GetMapping("/download")
    fun downloadByDate(@RequestParam date: LocalDate): ResponseEntity<Map<String, String>> {
        val url = attendanceHistoryService.generateExcelAndGetUrl(date)
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
