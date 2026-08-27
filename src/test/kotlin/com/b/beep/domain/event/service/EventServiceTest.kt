package com.b.beep.domain.event.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.event.controller.dto.request.CreateEventRequest
import com.b.beep.domain.event.controller.dto.request.UpdateEventRequest
import com.b.beep.domain.event.domain.EventMemoWriter
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.event.domain.entity.EventCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventUserEntity
import com.b.beep.domain.event.repository.EventCheckpointRepository
import com.b.beep.domain.event.repository.EventRepository
import com.b.beep.domain.event.repository.EventUserRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    @Mock private lateinit var eventRepository: EventRepository
    @Mock private lateinit var eventUserRepository: EventUserRepository
    @Mock private lateinit var eventCheckpointRepository: EventCheckpointRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var checkpointRepository: AttendanceCheckpointRepository
    @Mock private lateinit var attendanceRepository: AttendanceRepository
    @Mock private lateinit var attendanceTypeService: AttendanceTypeService
    @Mock private lateinit var studentInfoRepository: StudentInfoRepository
    @Mock private lateinit var eventMemoWriter: EventMemoWriter
    @Mock private lateinit var contextHolder: ContextHolder

    @InjectMocks private lateinit var eventService: EventService

    private val date: LocalDate = LocalDate.of(2026, 8, 26)
    private val eventType = AttendanceTypeEntity(id = 99L, name = AttendanceTypeEntity.SCHOOL_EVENT_TYPE_NAME)
    private val teacher = UserEntity(id = 500L, username = "teacher", name = "천준범", role = UserRole.TEACHER)

    private fun student(id: Long) =
        UserEntity(id = id, username = "s$id", name = "학생$id", role = UserRole.STUDENT)

    private fun checkpoint(id: Long, name: String) = AttendanceCheckpointEntity(
        id = id,
        name = name,
        startAt = LocalTime.of(16, 30),
        endAt = LocalTime.of(18, 59),
        attendanceStartAt = LocalTime.of(14, 0),
        attendanceEndAt = LocalTime.of(18, 39)
    )

    private fun info(user: UserEntity, grade: Int) =
        StudentInfoEntity(id = user.id, user = user, grade = grade, classNumber = 1, num = 1)

    private fun savedEvent(id: Long = 7L) =
        EventEntity(id = id, name = "체육대회", date = date, createdBy = teacher)

    @Nested
    @DisplayName("행사 등록")
    inner class CreateEvent {

        @Test
        @DisplayName("학생 수 × 교시 수 만큼 교내 행사 출석을 미리 만든다")
        fun createsAttendancePerStudentAndCheckpoint() {
            val students = listOf(student(11L), student(12L))
            val checkpoints = listOf(checkpoint(1L, "8~9교시"), checkpoint(2L, "10~11교시"))
            stubCreate(students, checkpoints)

            eventService.createEvent(CreateEventRequest("체육대회", date, listOf(1L, 2L), listOf(11L, 12L)))

            val captor = argumentCaptor<List<AttendanceEntity>>()
            verify(attendanceRepository).saveAll(captor.capture())
            val saved = captor.lastValue
            assertEquals(4, saved.size)
            assertTrue(saved.all { it.type.name == AttendanceTypeEntity.SCHOOL_EVENT_TYPE_NAME })
            assertTrue(saved.all { it.event != null })
            assertTrue(saved.all { it.room == null })
        }

        @Test
        @DisplayName("참여 학생이 속한 학년의 메모만 갱신한다")
        fun refreshesMemoForParticipatingGradesOnly() {
            val first = student(11L)
            val second = student(21L)
            stubCreate(listOf(first, second), listOf(checkpoint(1L, "8~9교시")), grades = mapOf(first to 1, second to 2))

            eventService.createEvent(CreateEventRequest("체육대회", date, listOf(1L), listOf(11L, 21L)))

            verify(eventMemoWriter).refresh(eq(date), eq(setOf(1, 2)))
        }

        @Test
        @DisplayName("없는 학생 ID가 섞이면 등록을 거부한다")
        fun rejectsUnknownStudent() {
            `when`(userRepository.findAllByIdInAndIsDeletedFalse(listOf(11L, 99L)))
                .thenReturn(listOf(student(11L)))

            val thrown = assertThrows(CustomException::class.java) {
                eventService.createEvent(CreateEventRequest("체육대회", date, listOf(1L), listOf(11L, 99L)))
            }
            assertEquals(UserError.USER_NOT_FOUND.name, thrown.code)
        }
    }

    @Nested
    @DisplayName("행사 수정")
    inner class UpdateEvent {

        @Test
        @DisplayName("빠진 학년의 메모도 함께 갱신하도록 변경 전후 학년을 합쳐 넘긴다")
        fun refreshesUnionOfGradesBeforeAndAfter() {
            val leaving = student(11L)
            val staying = student(21L)
            val event = savedEvent()

            `when`(eventRepository.findById(7L)).thenReturn(java.util.Optional.of(event))
            `when`(eventUserRepository.findAllByEventId(7L))
                .thenReturn(listOf(EventUserEntity(id = 1L, event = event, user = leaving)))
            `when`(studentInfoRepository.findAllByUserIn(listOf(leaving))).thenReturn(listOf(info(leaving, 1)))
            stubLookups(listOf(staying), listOf(checkpoint(1L, "8~9교시")), grades = mapOf(staying to 2))

            eventService.updateEvent(7L, UpdateEventRequest("체육대회", date, listOf(1L), listOf(21L)))

            verify(eventMemoWriter).refresh(eq(date), eq(setOf(1, 2)))
        }
    }

    @Nested
    @DisplayName("행사 삭제")
    inner class DeleteEvent {

        @Test
        @DisplayName("행사가 만든 출석을 지우고 참여 학년 메모를 갱신한다")
        fun removesAttendanceAndRefreshesMemo() {
            val participant = student(11L)
            val event = savedEvent()

            `when`(eventRepository.findById(7L)).thenReturn(java.util.Optional.of(event))
            `when`(eventUserRepository.findAllByEventId(7L))
                .thenReturn(listOf(EventUserEntity(id = 1L, event = event, user = participant)))
            `when`(studentInfoRepository.findAllByUserIn(listOf(participant))).thenReturn(listOf(info(participant, 1)))

            eventService.deleteEvent(7L)

            verify(attendanceRepository).deleteAllByEvent(event)
            verify(eventRepository).delete(event)
            verify(eventMemoWriter).refresh(eq(date), eq(setOf(1)))
        }
    }


    @Nested
    @DisplayName("행사 조회")
    inner class ReadEvents {

        @Test
        @DisplayName("교시 이름은 시작 시각 순으로 정렬하고 참여 인원은 행사별로 센다")
        fun sortsCheckpointsAndCountsStudents() {
            val event = savedEvent()
            val late = checkpoint(2L, "10~11교시").apply { startAt = LocalTime.of(19, 0) }
            val early = checkpoint(1L, "8~9교시")

            `when`(eventRepository.findAllByDateOrderByIdAsc(date)).thenReturn(listOf(event))
            // 늦은 교시를 앞에 넣어도 응답에서는 시작 시각 순으로 나와야 한다
            `when`(eventCheckpointRepository.findAllByEventIdIn(listOf(7L))).thenReturn(
                listOf(
                    EventCheckpointEntity(id = 1L, event = event, checkpoint = late),
                    EventCheckpointEntity(id = 2L, event = event, checkpoint = early)
                )
            )
            `when`(eventUserRepository.findAllByEventIdIn(listOf(7L))).thenReturn(
                listOf(
                    EventUserEntity(id = 1L, event = event, user = student(11L)),
                    EventUserEntity(id = 2L, event = event, user = student(12L))
                )
            )

            val responses = eventService.getEvents(date)

            assertEquals(1, responses.size)
            assertEquals(listOf("8~9교시", "10~11교시"), responses[0].checkpointNames)
            assertEquals(2, responses[0].studentCount)
            assertEquals("천준범", responses[0].createdByName)
        }

        @Test
        @DisplayName("그날 행사가 없으면 빈 목록을 준다")
        fun returnsEmptyWhenNoEvents() {
            `when`(eventRepository.findAllByDateOrderByIdAsc(date)).thenReturn(emptyList())

            assertTrue(eventService.getEvents(date).isEmpty())
        }

        @Test
        @DisplayName("상세 조회는 학번 순으로 정렬한 학생 명단을 준다")
        fun sortsStudentsByStudentNumber() {
            val event = savedEvent()
            val second = student(21L)
            val first = student(11L)

            `when`(eventRepository.findById(7L)).thenReturn(java.util.Optional.of(event))
            `when`(eventCheckpointRepository.findAllByEventId(7L)).thenReturn(
                listOf(EventCheckpointEntity(id = 1L, event = event, checkpoint = checkpoint(1L, "8~9교시")))
            )
            `when`(eventUserRepository.findAllByEventId(7L)).thenReturn(
                listOf(
                    EventUserEntity(id = 1L, event = event, user = second),
                    EventUserEntity(id = 2L, event = event, user = first)
                )
            )
            `when`(studentInfoRepository.findAllByUserIn(listOf(second, first))).thenReturn(
                listOf(info(second, 2), info(first, 1))
            )

            val detail = eventService.getEvent(7L)

            assertEquals(listOf("1101", "2101"), detail.students.map { it.studentId })
            assertEquals(listOf("8~9교시"), detail.checkpoints.map { it.name })
        }
    }

    /** 등록·수정이 공통으로 쓰는 조회 스텁. 등록 전용 스텁은 stubCreationOnly가 따로 맡는다. */
    private fun stubLookups(
        students: List<UserEntity>,
        checkpoints: List<AttendanceCheckpointEntity>,
        grades: Map<UserEntity, Int> = students.associateWith { 1 }
    ) {
        `when`(userRepository.findAllByIdInAndIsDeletedFalse(any())).thenReturn(students)
        `when`(checkpointRepository.findAllByIdInAndIsDeletedFalse(any())).thenReturn(checkpoints)
        `when`(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.SCHOOL_EVENT_TYPE_NAME))
            .thenReturn(eventType)
        `when`(studentInfoRepository.findAllByUserIn(students))
            .thenReturn(students.map { info(it, grades.getValue(it)) })
    }

    private fun stubCreationOnly() {
        `when`(contextHolder.user).thenReturn(teacher)
        // 실제 JPA가 저장 시 id를 채워주는 것을 그대로 흉내낸다
        `when`(eventRepository.save(any<EventEntity>())).thenAnswer {
            val saving = it.arguments[0] as EventEntity
            EventEntity(id = 7L, name = saving.name, date = saving.date, createdBy = saving.createdBy)
        }
    }

    private fun stubCreate(
        students: List<UserEntity>,
        checkpoints: List<AttendanceCheckpointEntity>,
        grades: Map<UserEntity, Int> = students.associateWith { 1 }
    ) {
        stubLookups(students, checkpoints, grades)
        stubCreationOnly()
    }
}
