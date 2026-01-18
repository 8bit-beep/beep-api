package com.b.beep.domain.memo.controller.docs

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid

@Tag(name = "메모")
interface MemoDocs {
    @Operation(summary = "메모 생성")
    fun createMemo(@Valid request: CreateMemoRequest)

    @Operation(summary = "메모 수정")
    fun updateMemo(@Valid request: UpdateMemoRequest)

    @Operation(summary = "메모 조회")
    fun getMemo(): MemoResponse
}
