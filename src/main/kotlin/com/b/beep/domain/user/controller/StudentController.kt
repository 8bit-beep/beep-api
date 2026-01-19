package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.service.StudentService
import com.b.beep.global.common.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "학생", description = "학생 API")
@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService
) {
    @Operation(summary = "학생 검색", description = "학년, 반, 이름으로 학생을 검색합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getStudents(
        @Parameter(description = "학년") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반") @RequestParam(required = false) classNumber: Int?,
        @Parameter(description = "이름 검색 키워드") @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 20) pageable: Pageable
    ): PageResponse<StudentResponse> {
        return PageResponse.from(studentService.findStudents(grade, classNumber, keyword, pageable))
    }
}
