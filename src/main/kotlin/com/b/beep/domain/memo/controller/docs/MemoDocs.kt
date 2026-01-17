package com.b.beep.domain.memo.controller.docs

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "메모")
interface MemoDocs {
    @Operation(summary = "메모 생성")
    fun createMemo(@RequestBody request: CreateMemoRequest)

    @Operation(summary = "메모 수정")
    fun updateMemo(@RequestBody request: UpdateMemoRequest)

    @Operation(summary = "메모 조회")
    fun getMemo(): MemoResponse
}
