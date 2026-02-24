package com.b.beep.domain.memo.controller.dto.response

import com.b.beep.domain.memo.domain.entity.MemoEntity

data class MemoResponse(
    val content: String,
    val isRead: Boolean
) {
    companion object {
        fun of(entity: MemoEntity): MemoResponse {
            return MemoResponse(
                content = entity.content,
                isRead = entity.isRead
            )
        }
    }
}