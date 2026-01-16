package com.b.beep.domain.memo.controller

import com.b.beep.domain.memo.controller.docs.MemoDocs
import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import com.b.beep.domain.memo.service.MemoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/memos")
class MemoController(
    private val memoService: MemoService
) : MemoDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createMemo(@RequestBody request: CreateMemoRequest) {
        memoService.createMemo(request)
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    override fun updateMemo(@RequestBody request: UpdateMemoRequest) {
        memoService.updateMemo(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getMemo(): MemoResponse {
        return MemoResponse.of(memoService.getMemo())
    }
}