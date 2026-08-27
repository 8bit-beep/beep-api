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

import org.mockito.kotlin.argumentCaptor

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
        content = listOf(eventBlock, manualContent).filter { it.isNotBlank() }.joinToString("\n\n")
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
            assertEquals("8~9교시 체육대회 (3명 참여) - 천준범\n\n내일 시험이니 조용히", existing.content)
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

            memoService.updateMemo(1, UpdateMemoRequest("$block\n\n새 메모"))

            assertEquals("새 메모", existing.manualContent)
            assertEquals("$block\n\n새 메모", existing.content)
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
}
