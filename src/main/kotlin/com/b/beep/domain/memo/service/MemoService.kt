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
        val memo = getOrCreateMemo(grade)
        memo.manualContent = request.content
        memo.isRead = false
        save(memo)
    }

    fun updateMemo(grade: Int, request: UpdateMemoRequest) {
        val memo = getMemoEntityByGrade(grade)
        memo.manualContent = stripEventBlock(request.newContent, memo.eventBlock)
        memo.isRead = false
        save(memo)
    }

    /**
     * 행사 도메인이 메모를 건드리는 유일한 창구.
     * 교사가 쓴 manualContent는 손대지 않고 자동 영역만 교체한다.
     */
    fun replaceEventBlock(grade: Int, block: String) {
        val memo = getOrCreateMemo(grade)
        memo.eventBlock = block
        save(memo)
    }

    fun getMemo(grade: Int): MemoResponse {
        val memo = getMemoEntityByGrade(grade)
        memo.isRead = true
        memoRepository.save(memo)
        return MemoResponse.of(memo)
    }

    private fun save(memo: MemoEntity) {
        memo.content = listOf(memo.eventBlock, memo.manualContent)
            .filter { it.isNotBlank() }
            .joinToString(BLOCK_SEPARATOR)
        memoRepository.save(memo)
    }

    /**
     * 교사는 자동 영역까지 포함된 전체 텍스트를 보내온다.
     * 앞머리의 행사 블록을 떼어낸 나머지가 수기 영역이다.
     */
    private fun stripEventBlock(incoming: String, eventBlock: String): String {
        if (eventBlock.isBlank()) return incoming
        return incoming.removePrefix(eventBlock).trimStart('\n')
    }

    private fun getOrCreateMemo(grade: Int): MemoEntity {
        return memoRepository.findByGrade(grade) ?: MemoEntity(grade = grade)
    }

    private fun getMemoEntityByGrade(grade: Int): MemoEntity {
        return memoRepository.findByGrade(grade)
            ?: throw CustomException(MemoError.MEMO_NOT_FOUND)
    }

    companion object {
        private const val BLOCK_SEPARATOR = "\n\n"
    }
}
