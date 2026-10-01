package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.ScanHelpQrRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.attendance.repository.HelpQrTokenData
import com.b.beep.domain.attendance.repository.HelpQrTokenRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.security.ContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class AttendanceHelpServiceTest {
    @Mock private lateinit var helpQrTokenRepository: HelpQrTokenRepository
    @Mock private lateinit var attendanceRepository: AttendanceRepository
    @Mock private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository
    @Mock private lateinit var studentScheduleRepository: StudentScheduleRepository
    @Mock private lateinit var studentInfoRepository: StudentInfoRepository
    @Mock private lateinit var checkpointResolver: CheckpointResolver
    @Mock private lateinit var contextHolder: ContextHolder
    @Mock private lateinit var roomService: RoomService
    @Mock private lateinit var attendanceTypeService: AttendanceTypeService
    @InjectMocks private lateinit var service: AttendanceHelpService

    private val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
    private val student = UserEntity(id = 11L, username = "student", name = "학생", role = UserRole.STUDENT)
    private val checkpoint = AttendanceCheckpointEntity(
        id = 2L, name = "10~11교시",
        startAt = LocalTime.of(19, 0), endAt = LocalTime.of(20, 39),
        attendanceStartAt = LocalTime.of(19, 0), attendanceEndAt = LocalTime.of(19, 19)
    )
    private val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
    private val room = RoomEntity(id = 10L, name = "동아리실")
    private val originalType = AttendanceTypeEntity(id = 2L, name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)
    private val originalRoom = RoomEntity(id = 20L, name = "1-2")
    private val notAttended = AttendanceTypeEntity(id = 3L, name = AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME)
    private val attendance = AttendanceEntity(id = 1L, user = student, checkpoint = checkpoint, date = today, type = notAttended)
    private val request = ScanHelpQrRequest(token = "help-token", typeId = club.id!!)

    @BeforeEach
    fun setUp() {
        whenever(contextHolder.user).thenReturn(student)
        whenever(helpQrTokenRepository.findByToken(request.token))
            .thenReturn(HelpQrTokenData(helperId = 99L, checkpointId = checkpoint.id!!, roomId = room.id!!))
        whenever(studentInfoRepository.findByUser(student))
            .thenReturn(StudentInfoEntity(id = 1L, user = student, grade = 1, classNumber = 2, num = 3))
        whenever(checkpointResolver.getCurrentAttendableCheckpoint(1)).thenReturn(checkpoint)
        whenever(roomService.getRoomEntityById(room.id!!)).thenReturn(room)
        whenever(attendanceTypeService.getAttendanceTypeEntityById(club.id!!)).thenReturn(club)
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME))
            .thenReturn(notAttended)
        whenever(attendanceRepository.findByUserIdAndCheckpointIdAndDate(student.id!!, checkpoint.id!!, today))
            .thenReturn(attendance)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `디폴트 QR 출석은 스캔한 학생의 스케줄을 생성하거나 갱신한다`(scheduleExists: Boolean) {
        val existing = if (scheduleExists) originalSchedule() else null
        whenever(studentScheduleRepository.findByUserAndDayOfWeekAndCheckpoint(student, today.dayOfWeek, checkpoint))
            .thenReturn(existing)
        whenever(studentScheduleRepository.save(any<StudentScheduleEntity>())).thenAnswer { it.getArgument(0) }

        service.scanHelpQr(request)

        val scheduleCaptor = argumentCaptor<StudentScheduleEntity>()
        verify(studentScheduleRepository).save(scheduleCaptor.capture())
        val saved = scheduleCaptor.firstValue
        if (existing != null) assertSame(existing, saved)
        assertSame(student, saved.user)
        assertEquals(today.dayOfWeek, saved.dayOfWeek)
        assertSame(checkpoint, saved.checkpoint)
        assertSame(club, saved.type)
        assertSame(room, saved.room)
        verify(attendanceSortModeRepository).existsByDateAndCheckpointAndGrade(today, checkpoint, 1)
        assertAttendanceSaved()
    }

    @Test
    fun `변경 모드 QR 출석은 스케줄을 조회 생성 수정하지 않고 출석을 저장한다`() {
        whenever(attendanceSortModeRepository.existsByDateAndCheckpointAndGrade(today, checkpoint, 1)).thenReturn(true)

        service.scanHelpQr(request)

        verifyNoInteractions(studentScheduleRepository)
        verify(attendanceSortModeRepository).existsByDateAndCheckpointAndGrade(today, checkpoint, 1)
        assertAttendanceSaved()
    }

    @Test
    fun `변경 모드 QR 출석도 첫 출석 기록을 생성한다`() {
        whenever(attendanceSortModeRepository.existsByDateAndCheckpointAndGrade(today, checkpoint, 1)).thenReturn(true)
        whenever(attendanceRepository.findByUserIdAndCheckpointIdAndDate(student.id!!, checkpoint.id!!, today))
            .thenReturn(null)
        whenever(attendanceRepository.saveAndFlush(any<AttendanceEntity>())).thenAnswer { it.getArgument(0) }

        service.scanHelpQr(request)

        verifyNoInteractions(studentScheduleRepository)
        val attendanceCaptor = argumentCaptor<AttendanceEntity>()
        verify(attendanceRepository, times(2)).saveAndFlush(attendanceCaptor.capture())
        val saved = attendanceCaptor.lastValue
        assertSame(student, saved.user)
        assertSame(checkpoint, saved.checkpoint)
        assertEquals(today, saved.date)
        assertSame(club, saved.type)
        assertSame(room, saved.room)
    }

    private fun originalSchedule() = StudentScheduleEntity(
        id = 1L, user = student, dayOfWeek = today.dayOfWeek, checkpoint = checkpoint,
        type = originalType, room = originalRoom
    )

    private fun assertAttendanceSaved() {
        verify(attendanceRepository).saveAndFlush(attendance)
        assertSame(club, attendance.type)
        assertSame(room, attendance.room)
        assertEquals(today, attendance.date)
    }
}
