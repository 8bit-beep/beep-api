package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.StudentDocs
import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.service.StudentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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
    ): Page<StudentResponse> {
        return studentService.findAll(grade, classNumber, keyword, pageable)
    }
}
