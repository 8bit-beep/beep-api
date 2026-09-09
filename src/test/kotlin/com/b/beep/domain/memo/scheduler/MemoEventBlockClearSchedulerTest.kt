package com.b.beep.domain.memo.scheduler

import com.b.beep.domain.memo.service.MemoService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class MemoEventBlockClearSchedulerTest {

    @Mock
    private lateinit var memoService: MemoService

    @InjectMocks
    private lateinit var scheduler: MemoEventBlockClearScheduler

    @Test
    @DisplayName("스케줄이 돌면 모든 메모의 행사 블록을 비운다")
    fun delegatesToMemoService() {
        scheduler.clearExpiredEventBlocks()

        verify(memoService).clearAllEventBlocks()
    }
}
