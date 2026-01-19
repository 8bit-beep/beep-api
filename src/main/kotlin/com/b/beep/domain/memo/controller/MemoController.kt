package com.b.beep.domain.memo.controller

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import com.b.beep.domain.memo.service.MemoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "메모")
@Validated
@RestController
@RequestMapping("/memos")
class MemoController(
    private val memoService: MemoService
) {
    @Operation(summary = "메모 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMemo(@Valid @RequestBody request: CreateMemoRequest) {
        memoService.createMemo(request)
    }

    @Operation(summary = "메모 수정")
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    fun updateMemo(@Valid @RequestBody request: UpdateMemoRequest) {
        memoService.updateMemo(request)
    }

    @Operation(summary = "메모 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getMemo(): MemoResponse {
        return memoService.getMemo()
    }
}
