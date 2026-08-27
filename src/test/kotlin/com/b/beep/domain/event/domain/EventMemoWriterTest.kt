package com.b.beep.domain.event.domain

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.event.domain.entity.EventUserEntity
import com.b.beep.domain.event.repository.EventCheckpointRepository
import com.b.beep.domain.event.repository.EventRepository
import com.b.beep.domain.event.repository.EventUserRepository
import com.b.beep.domain.memo.service.MemoService
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("학년별 메모 갱신")
class EventMemoWriterTest {

    @Mock private lateinit var eventRepository: EventRepository
    @Mock private lateinit var eventUserRepository: EventUserRepository
    @Mock private lateinit var eventCheckpointRepository: EventCheckpointRepository
    @Mock private lateinit var studentInfoRepository: StudentInfoRepository
    @Mock private lateinit var memoService: MemoService

    private lateinit var writer: EventMemoWriter

    private val date: LocalDate = LocalDate.of(2026, 8, 26)
    private val teacher = UserEntity(id = 500L, username = "t", name = "천준범", role = UserRole.TEACHER)

    @BeforeEach
    fun setUp() {
        writer = EventMemoWriter(
            eventRepository,
            eventUserRepository,
            eventCheckpointRepository,
            studentInfoRepository,
            memoService,
            EventMemoComposer()
        )
    }

    private fun student(id: Long, name: String) =
        UserEntity(id = id, username = "s$id", name = name, role = UserRole.STUDENT)

    private fun info(user: UserEntity, grade: Int, classNumber: Int, num: Int) =
        StudentInfoEntity(id = user.id, user = user, grade = grade, classNumber = classNumber, num = num)

    private fun checkpoint(id: Long, name: String) = AttendanceCheckpointEntity(
        id = id, name = name,
        startAt = LocalTime.of(16, 30), endAt = LocalTime.of(18, 59),
        attendanceStartAt = LocalTime.of(14, 0), attendanceEndAt = LocalTime.of(18, 39)
    )

    @Test
    @DisplayName("학년마다 그 학년 학생만 담긴 블록을 만들어 메모에 넘긴다")
    fun buildsBlockPerGradeWithOwnStudentsOnly() {
        val kim = student(11L, "김철수")   // 1학년 1반 1번
        val lee = student(21L, "이영희")   // 2학년 1반 1번
        val park = student(12L, "박민수")  // 1학년 1반 2번

        val sports = EventEntity(id = 1L, name = "체육대회", date = date, createdBy = teacher)
        val club = EventEntity(id = 2L, name = "동아리발표", date = date, createdBy = teacher)

        `when`(eventRepository.findAllByDateOrderByIdAsc(date)).thenReturn(listOf(sports, club))
        `when`(eventUserRepository.findAllByEventIdIn(listOf(1L, 2L))).thenReturn(
            listOf(
                EventUserEntity(id = 1L, event = sports, user = kim),
                EventUserEntity(id = 2L, event = sports, user = lee),
                EventUserEntity(id = 3L, event = club, user = park)
            )
        )
        `when`(eventCheckpointRepository.findAllByEventIdIn(listOf(1L, 2L))).thenReturn(
            listOf(
                EventCheckpointEntity(id = 1L, event = sports, checkpoint = checkpoint(1L, "8~9교시")),
                EventCheckpointEntity(id = 2L, event = club, checkpoint = checkpoint(2L, "10~11교시"))
            )
        )
        `when`(studentInfoRepository.findAllByUserIn(any())).thenReturn(
            listOf(info(kim, 1, 1, 1), info(lee, 2, 1, 1), info(park, 1, 1, 2))
        )

        writer.refresh(date, setOf(1, 2))

        verify(memoService).replaceEventBlock(
            eq(1),
            eq(
                """
                8월 26일
                8~9교시 체육대회 (1명 참여) - 천준범
                1101 김철수
                10~11교시 동아리발표 (1명 참여) - 천준범
                1102 박민수
                """.trimIndent()
            )
        )
        verify(memoService).replaceEventBlock(
            eq(2),
            eq(
                """
                8월 26일
                8~9교시 체육대회 (1명 참여) - 천준범
                2101 이영희
                """.trimIndent()
            )
        )
    }

    @Test
    @DisplayName("그날 행사가 하나도 없으면 빈 블록을 넘겨 메모의 자동 영역을 비운다")
    fun clearsBlockWhenNoEventsRemain() {
        `when`(eventRepository.findAllByDateOrderByIdAsc(date)).thenReturn(emptyList())

        writer.refresh(date, setOf(1))

        verify(memoService).replaceEventBlock(1, "")
    }

    @Test
    @DisplayName("갱신할 학년이 없으면 메모를 건드리지 않는다")
    fun doesNothingWhenNoGrades() {
        writer.refresh(date, emptySet())

        verify(memoService, never()).replaceEventBlock(any(), any())
    }
}
