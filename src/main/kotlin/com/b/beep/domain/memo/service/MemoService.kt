package com.b.beep.domain.memo.service

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.controller.dto.response.MemoResponse
import com.b.beep.domain.memo.domain.entity.MemoEntity
import com.b.beep.domain.memo.error.MemoError
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemoService(
    private val memoRepository: MemoRepository
) {
    fun createMemo(request: CreateMemoRequest) {
        val memo = MemoEntity(
            content = request.content
        )
        memoRepository.save(memo)
    }

    fun updateMemo(request: UpdateMemoRequest) {
        val memo = getMemoEntity()
        memo.content = request.newContent
        memo.isRead = false
        memoRepository.save(memo)
    }

    fun getMemo(): MemoResponse {
        val memo = getMemoEntity()
        memo.isRead = true
        memoRepository.save(memo)
        return MemoResponse.of(memo)
    }

    private fun getMemoEntity(): MemoEntity {
        return memoRepository.findAll().firstOrNull()
            ?: throw CustomException(MemoError.MEMO_NOT_FOUND)
    }
}
