package com.b.beep.domain.memo.controller

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import com.b.beep.domain.memo.service.MemoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    @PostMapping("/{grade}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMemo(
        @PathVariable @Min(1, message = "학년은 1 이상이어야 합니다") @Max(3, message = "학년은 3 이하여야 합니다") grade: Int,
        @Valid @RequestBody request: CreateMemoRequest
    ) {
        memoService.createMemo(grade, request)
    }

    @Operation(summary = "메모 수정")
    @PatchMapping("/{grade}")
    @ResponseStatus(HttpStatus.OK)
    fun updateMemo(
        @PathVariable @Min(1, message = "학년은 1 이상이어야 합니다") @Max(3, message = "학년은 3 이하여야 합니다") grade: Int,
        @Valid @RequestBody request: UpdateMemoRequest
    ) {
        memoService.updateMemo(grade, request)
    }

    @Operation(summary = "메모 조회")
    @GetMapping("/{grade}")
    @ResponseStatus(HttpStatus.OK)
    fun getMemo(
        @PathVariable @Min(1, message = "학년은 1 이상이어야 합니다") @Max(3, message = "학년은 3 이하여야 합니다") grade: Int
    ): MemoResponse {
        return memoService.getMemo(grade)
    }
}
