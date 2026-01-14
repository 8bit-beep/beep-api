package com.b.beep.dev

import com.b.beep.domain.approval.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.memo.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.domain.period.repository.PeriodRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

@Configuration
class DataGenerator(
    private val memoRepository: MemoRepository,
    private val approvalRepository: ApprovalRepository,
    private val periodRepository: PeriodRepository,
) {
    @Bean
    fun initializeData(): ApplicationRunner {
        return ApplicationRunner {
            if (memoRepository.count() == 0L) {
                println("--- 데이터 생성 시작 ---")

                memoRepository.save(
                    MemoEntity(
                        content = "메모 내용"
                    )
                )

                println("--- 데이터 생성 완료 ---")
            } else {
                println("데이터가 이미 존재하여 생성하지 않습니다.")
            }

            if (approvalRepository.count() == 0L) {
                val periods = periodRepository.findAll().map { it.period }
                val rooms = Room.entries.toTypedArray()
                val today = LocalDate.now()
                val approvals = rooms.flatMap { room ->
                    periods.map { period ->
                        ApprovalEntity(
                            room = room,
                            period = period,
                            date = today
                        )
                    }
                }
                approvalRepository.saveAll(approvals)
            }
        }
    }
}