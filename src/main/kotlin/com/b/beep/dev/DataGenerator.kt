package com.b.beep.dev

import com.b.beep.domain.memo.domain.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataGenerator(
    private val memoRepository: MemoRepository,
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
                val defaultRooms = listOf(
                    "NOTFOUND", "OTHER",
                    "PROJECT1", "PROJECT2", "PROJECT3", "PROJECT4", "PROJECT5", "PROJECT6",
                    "LAB1", "LAB2", "LAB6_7", "LAB8_9", "LAB10_11", "LAB13", "LAB17_18", "LAB19_20", "LAB21_22"
                )
                roomRepository.saveAll(defaultRooms.map { RoomEntity(name = it) })
            }
        }
    }
}