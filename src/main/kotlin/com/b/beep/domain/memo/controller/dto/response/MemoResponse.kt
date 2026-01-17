package com.b.beep.domain.memo.controller.dto.response

import com.b.beep.domain.memo.domain.entity.MemoEntity

data class MemoResponse(
    val content: String,
) {
    companion object {
        fun of(memo: MemoEntity): MemoResponse {
            return MemoResponse(
                content = memo.content,
            )
        }
    }
}