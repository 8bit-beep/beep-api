package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.AttendanceHistoryDocs
import com.b.beep.domain.attendance.service.AttendanceHistoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/attendances/histories")
class AttendanceHistoryController(
    private val attendanceHistoryService: AttendanceHistoryService,
) : AttendanceHistoryDocs {
    @GetMapping
    override fun listFiles(): List<String> {
        return attendanceHistoryService.listFiles()
    }
//
//    @GetMapping("/uploads")
//    fun listUploads(): List<String> {
//        return attendanceHistoryService.listFiles("uploads/")
//    }

    @GetMapping("/download")
    override fun downloadByDate(@RequestParam date: LocalDate): ResponseEntity<Map<String, String>> {
        val key = "uploads/attendance_$date.xlsx"
        if (!attendanceHistoryService.exists(key)) {
            return ResponseEntity.notFound().build()
        }
        val url = attendanceHistoryService.generatePresignedUrl(key)
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
