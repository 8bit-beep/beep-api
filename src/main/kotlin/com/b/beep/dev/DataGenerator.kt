package com.b.beep.dev

import com.b.beep.domain.memo.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.domain.room.approval.entity.ApprovalEntity
import com.b.beep.domain.room.approval.repository.ApprovalRepository
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

@Configuration
class DataGenerator(
    private val memoRepository: MemoRepository,
    private val approvalRepository: ApprovalRepository,
    private val roomRepository: RoomRepository,
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

            if (roomRepository.count() == 0L) {
                println("--- Room 데이터 생성 시작 ---")

                val rooms = listOf(
                    RoomEntity(name = "NOTFOUND"),
                    RoomEntity(name = "OTHER"),
                    RoomEntity(name = "PROJECT1"),
                    RoomEntity(name = "PROJECT2"),
                    RoomEntity(name = "PROJECT3"),
                    RoomEntity(name = "PROJECT4"),
                    RoomEntity(name = "PROJECT5"),
                    RoomEntity(name = "PROJECT6"),
                    RoomEntity(name = "LAB1"),
                    RoomEntity(name = "LAB2"),
                    RoomEntity(name = "LAB6_7"),
                    RoomEntity(name = "LAB8_9"),
                    RoomEntity(name = "LAB10_11"),
                    RoomEntity(name = "LAB13"),
                    RoomEntity(name = "LAB17_18"),
                    RoomEntity(name = "LAB19_20"),
                    RoomEntity(name = "LAB21_22"),
                )
                roomRepository.saveAll(rooms)

                println("--- Room 데이터 생성 완료 ---")
            }

            if (approvalRepository.count() == 0L) {
                val periods = listOf(1, 2, 3)
                val rooms = roomRepository.findAll()
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