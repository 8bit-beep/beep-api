package com.b.beep.domain.memo.service

import com.b.beep.domain.memo.controller.dto.request.CreateMemoRequest
import com.b.beep.domain.memo.controller.dto.request.UpdateMemoRequest
import com.b.beep.domain.memo.domain.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class MemoServiceTest {

    @Mock
    private lateinit var memoRepository: MemoRepository

    @InjectMocks
    private lateinit var memoService: MemoService

    private fun memo(
        grade: Int = 1,
        eventBlock: String = "",
        manualContent: String = ""
    ) = MemoEntity(
        id = 1L,
        grade = grade,
        eventBlock = eventBlock,
        manualContent = manualContent,
        content = listOf(manualContent, eventBlock).filter { it.isNotBlank() }.joinToString("\n\n")
    )

    @Nested
    @DisplayName("행사 블록 교체")
    inner class ReplaceEventBlock {

        @Test
        @DisplayName("해당 학년 메모가 없으면 새로 만들어 블록을 넣는다")
        fun createsMemoWhenAbsent() {
            `when`(memoRepository.findByGrade(1)).thenReturn(null)

            memoService.replaceEventBlock(1, "8~9교시 체육대회 (3명 참여) - 천준범")

            val captor = argumentCaptor<MemoEntity>()
            verify(memoRepository).save(captor.capture())
            assertEquals(1, captor.lastValue.grade)
            assertEquals("8~9교시 체육대회 (3명 참여) - 천준범", captor.lastValue.content)
        }

        @Test
        @DisplayName("교사가 쓴 수기 메모는 그대로 두고 행사 블록만 바꾼다")
        fun preservesManualContent() {
            val existing = memo(eventBlock = "8~9교시 옛행사 (1명 참여) - 김지영", manualContent = "내일 시험이니 조용히")
            `when`(memoRepository.findByGrade(1)).thenReturn(existing)

            memoService.replaceEventBlock(1, "8~9교시 체육대회 (3명 참여) - 천준범")

            assertEquals("내일 시험이니 조용히", existing.manualContent)
            assertEquals("내일 시험이니 조용히\n\n8~9교시 체육대회 (3명 참여) - 천준범", existing.content)
        }

        @Test
        @DisplayName("수기가 없으면 content는 행사 블록만이다")
        fun contentIsOnlyEventBlockWhenManualIsEmpty() {
            `when`(memoRepository.findByGrade(1)).thenReturn(null)

            memoService.replaceEventBlock(1, "8~9교시 체육대회 (3명 참여) - 천준범")

            val captor = argumentCaptor<MemoEntity>()
            verify(memoRepository).save(captor.capture())
            assertEquals("8~9교시 체육대회 (3명 참여) - 천준범", captor.lastValue.content)
            assertEquals("", captor.lastValue.manualContent)
        }

        @Test
        @DisplayName("행사가 모두 삭제되어 빈 블록이 오면 수기 메모만 남는다")
        fun leavesOnlyManualContentWhenBlockIsEmpty() {
            val existing = memo(eventBlock = "8~9교시 체육대회 (3명 참여) - 천준범", manualContent = "내일 시험이니 조용히")
            `when`(memoRepository.findByGrade(1)).thenReturn(existing)

            memoService.replaceEventBlock(1, "")

            assertEquals("내일 시험이니 조용히", existing.content)
        }
    }

    @Nested
    @DisplayName("메모 수정")
    inner class UpdateMemo {

        @Test
        @DisplayName("받은 전체 텍스트에서 행사 블록을 떼어내고 수기 부분만 저장한다")
        fun stripsEventBlockFromIncomingText() {
            val block = "8~9교시 체육대회 (3명 참여) - 천준범"
            val existing = memo(eventBlock = block, manualContent = "옛 메모")
            `when`(memoRepository.findByGrade(1)).thenReturn(existing)

            memoService.updateMemo(1, UpdateMemoRequest("새 메모\n\n$block"))

            assertEquals("새 메모", existing.manualContent)
            assertEquals("새 메모\n\n$block", existing.content)
        }
    }

    @Nested
    @DisplayName("메모 생성")
    inner class CreateMemo {

        @Test
        @DisplayName("같은 학년 메모가 이미 있으면 새 메모를 만들지 않고 내용을 갱신한다")
        fun updatesInsteadOfInsertingDuplicate() {
            val existing = memo(manualContent = "옛 메모")
            `when`(memoRepository.findByGrade(1)).thenReturn(existing)

            memoService.createMemo(1, CreateMemoRequest("새 메모"))

            val captor = argumentCaptor<MemoEntity>()
            verify(memoRepository).save(captor.capture())
            assertSame(existing, captor.lastValue)
            assertEquals("새 메모", existing.manualContent)
        }
    }

    @Nested
    @DisplayName("행사 블록 일일 만료")
    inner class ClearAllEventBlocks {

        @Test
        @DisplayName("수기 메모는 남기고 행사 블록만 비운다")
        fun clearsEventBlockAndKeepsManualContent() {
            val existing = memo(eventBlock = "8~9교시 체육대회 (3명 참여) - 천준범", manualContent = "내일 시험이니 조용히")
            `when`(memoRepository.findAll()).thenReturn(listOf(existing))

            memoService.clearAllEventBlocks()

            assertEquals("", existing.eventBlock)
            assertEquals("내일 시험이니 조용히", existing.manualContent)
            assertEquals("내일 시험이니 조용히", existing.content)
            verify(memoRepository).save(existing)
        }

        @Test
        @DisplayName("행사 블록이 이미 비어 있으면 저장하지 않는다")
        fun skipsSaveWhenEventBlockIsBlank() {
            val existing = memo(manualContent = "내일 시험이니 조용히")
            `when`(memoRepository.findAll()).thenReturn(listOf(existing))

            memoService.clearAllEventBlocks()

            verify(memoRepository, never()).save(any())
        }

        @Test
        @DisplayName("메모가 없으면 아무 일도 하지 않는다")
        fun doesNothingWhenNoMemosExist() {
            `when`(memoRepository.findAll()).thenReturn(emptyList())

            memoService.clearAllEventBlocks()

            verify(memoRepository, never()).save(any())
        }

        @Test
        @DisplayName("여러 학년 중 행사 블록이 있는 학년만 비운다")
        fun clearsOnlyGradesWithEventBlock() {
            val withBlock = memo(grade = 1, eventBlock = "8~9교시 체육대회 (3명 참여) - 천준범", manualContent = "1학년 메모")
            val withoutBlock = memo(grade = 2, manualContent = "2학년 메모")
            `when`(memoRepository.findAll()).thenReturn(listOf(withBlock, withoutBlock))

            memoService.clearAllEventBlocks()

            assertEquals("", withBlock.eventBlock)
            assertEquals("1학년 메모", withBlock.content)
            verify(memoRepository).save(withBlock)
            verify(memoRepository, never()).save(withoutBlock)
        }
    }
}
