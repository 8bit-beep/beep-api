package com.b.beep.domain.period.controller.docs

import com.b.beep.domain.period.controller.dto.request.CreatePeriodRequest
import com.b.beep.domain.period.controller.dto.request.UpdatePeriodRequest
import com.b.beep.domain.period.controller.dto.response.PeriodResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "교시 설정")
interface PeriodDocs {
    @Operation(summary = "전체 교시 조회")
    fun findAll(): List<PeriodResponse>

    @Operation(summary = "교시 조회")
    fun findByPeriod(period: Int): PeriodResponse

    @Operation(summary = "교시 생성")
    fun create(request: CreatePeriodRequest)

    @Operation(summary = "교시 수정")
    fun update(period: Int, request: UpdatePeriodRequest)

    @Operation(summary = "교시 삭제")
    fun delete(period: Int)
}
