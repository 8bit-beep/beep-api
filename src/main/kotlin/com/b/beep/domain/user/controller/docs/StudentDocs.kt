package com.b.beep.domain.user.controller.docs

import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.global.common.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable

@Tag(name = "학생", description = "학생 API")
interface StudentDocs {
    @Operation(summary = "학생 검색", description = "학년, 반, 이름으로 학생을 검색합니다.")
    fun getStudents(
        @Parameter(description = "학년") grade: Int?,
        @Parameter(description = "반") classNumber: Int?,
        @Parameter(description = "이름 검색 키워드") keyword: String?,
        pageable: Pageable
    ): PageResponse<StudentResponse>
}
