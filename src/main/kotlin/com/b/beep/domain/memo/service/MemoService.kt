package com.b.beep.domain.memo.service

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
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
    fun create(request: CreateMemoRequest) {
        val memo = MemoEntity(
            content = request.content
        )
        memoRepository.save(memo)
    }

    fun update(request: UpdateMemoRequest) {
        val memo = get()
        memo.content = request.newContent
        memoRepository.save(memo)
    }

    @Transactional(readOnly = true)
    fun get(): MemoEntity {
        return memoRepository.findAll().firstOrNull()
            ?: throw CustomException(MemoError.MEMO_NOT_FOUND)
    }
}
