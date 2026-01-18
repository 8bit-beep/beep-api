package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.StudentDocs
import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.service.StudentService
import com.b.beep.global.common.dto.PageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService
) : StudentDocs {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getStudents(
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 20) pageable: Pageable
    ): PageResponse<StudentResponse> {
        return PageResponse.from(studentService.findStudents(grade, classNumber, keyword, pageable))
    }
}
