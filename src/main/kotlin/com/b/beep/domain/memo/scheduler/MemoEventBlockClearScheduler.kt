package com.b.beep.domain.memo.scheduler

import com.b.beep.domain.memo.service.MemoService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MemoEventBlockClearScheduler(
    private val memoService: MemoService
) {
    @Scheduled(cron = "0 50 21 * * *", zone = "Asia/Seoul")
    fun clearExpiredEventBlocks() {
        memoService.clearAllEventBlocks()
    }
}
