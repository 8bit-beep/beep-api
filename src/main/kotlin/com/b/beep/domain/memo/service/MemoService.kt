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
    fun createMemo(grade: Int, request: CreateMemoRequest) {
        val memo = MemoEntity(
            grade = grade,
            content = request.content
        )
        memoRepository.save(memo)
    }

    fun updateMemo(grade: Int, request: UpdateMemoRequest) {
        val memo = getMemoEntityByGrade(grade)
        memo.content = request.newContent
        memo.isRead = false
        memoRepository.save(memo)
    }

    fun getMemo(grade: Int): MemoResponse {
        val memo = getMemoEntityByGrade(grade)
        memo.isRead = true
        memoRepository.save(memo)
        return MemoResponse.of(memo)
    }

    private fun getMemoEntityByGrade(grade: Int): MemoEntity {
        return memoRepository.findByGrade(grade)
            ?: throw CustomException(MemoError.MEMO_NOT_FOUND)
    }
}
