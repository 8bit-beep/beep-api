package com.b.beep.dev

import com.b.beep.domain.approval.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.memo.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.domain.period.entity.PeriodEntity
import com.b.beep.domain.period.repository.PeriodRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalTime

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
                println("--- 메모 데이터 생성 시작 ---")

                memoRepository.save(
                    MemoEntity(
                        content = "메모 내용"
                    )
                )

                println("--- 메모 데이터 생성 완료 ---")
            }

            if (periodRepository.count() == 0L) {
                println("--- Period 데이터 생성 시작 ---")

                val periods = listOf(
                    PeriodEntity(
                        period = 1,
                        attendanceStartTime = LocalTime.of(9, 0),
                        attendanceEndTime = LocalTime.of(9, 20),
                        periodStartTime = LocalTime.of(9, 0),
                        periodEndTime = LocalTime.of(13, 19, 59),
                    ),
                    PeriodEntity(
                        period = 2,
                        attendanceStartTime = LocalTime.of(13, 20),
                        attendanceEndTime = LocalTime.of(13, 40),
                        periodStartTime = LocalTime.of(13, 20),
                        periodEndTime = LocalTime.of(18, 59, 59),
                    ),
                    PeriodEntity(
                        period = 3,
                        attendanceStartTime = LocalTime.of(19, 0),
                        attendanceEndTime = LocalTime.of(19, 20),
                        periodStartTime = LocalTime.of(19, 0),
                        periodEndTime = LocalTime.of(21, 40),
                    ),
                )
                periodRepository.saveAll(periods)

                println("--- Period 데이터 생성 완료 ---")
            }

            if (approvalRepository.count() == 0L) {
                val periods = listOf(1, 2, 3)
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