package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.dto.response.OutSleepingResponse
import com.b.beep.domain.absence.error.OutSleepingOpenApiError
import com.b.beep.domain.absence.service.OutSleepingOpenApiService
import com.b.beep.global.exception.CustomException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "큐빅 외박자 OpenAPI", description = "큐빅에 외박자 명단을 제공하는 외부 API")
@RestController
@RequestMapping("/out-sleeping/openapi")
class OutSleepingOpenApiController(
    private val outSleepingOpenApiService: OutSleepingOpenApiService,
) {
    @Operation(summary = "날짜별 외박자 명단 조회")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    fun searchOutSleepingStudents(
        @Parameter(required = true, description = "검색할 날짜(yyyy-MM-dd)")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): OutSleepingResponse {
        val targetDate = date ?: throw CustomException(OutSleepingOpenApiError.DATE_REQUIRED)
        return outSleepingOpenApiService.search(targetDate)
    }
}
