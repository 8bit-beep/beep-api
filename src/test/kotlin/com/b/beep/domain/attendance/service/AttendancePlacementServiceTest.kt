package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class AttendancePlacementServiceTest {

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository

    @Mock
    private lateinit var studentActivityRoomRepository: StudentActivityRoomRepository

    @Mock
    private lateinit var studentInfoRepository: StudentInfoRepository

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @Mock
    private lateinit var roomRepository: RoomRepository

    private lateinit var service: AttendancePlacementService
    private lateinit var student: UserEntity
    private lateinit var studentInfo: StudentInfoEntity
    private lateinit var checkpoint: AttendanceCheckpointEntity
    private lateinit var scheduleRoom: RoomEntity
    private lateinit var scheduleType: AttendanceTypeEntity

    @BeforeEach
    fun setUp() {
        service = AttendancePlacementService(
            attendanceRepository,
            attendanceSortModeRepository,
            studentActivityRoomRepository,
            studentInfoRepository,
            studentScheduleRepository,
            roomRepository
        )
        student = UserEntity(id = 1L, username = "student", name = "학생", role = UserRole.STUDENT)
        studentInfo = StudentInfoEntity(id = 1L, user = student, grade = 1, classNumber = 2, num = 3)
        checkpoint = AttendanceCheckpointEntity(
            id = 1L,
            name = "7~8교시",
            startAt = LocalTime.of(15, 20),
            endAt = LocalTime.of(17, 19),
            attendanceStartAt = LocalTime.of(15, 20),
            attendanceEndAt = LocalTime.of(15, 40),
            dayOfWeek = DayOfWeek.MONDAY,
            grade = 1
        )
        scheduleRoom = RoomEntity(id = 10L, name = "기존 스케줄실")
        scheduleType = AttendanceTypeEntity(id = 2L, name = "기타")
    }

    @Test
    fun `월요일 나르샤 모드는 요일 없는 공통 활동실을 사용한다`() {
        val narshaType = AttendanceTypeEntity(id = 7L, name = AttendanceTypeEntity.NARSHA_TYPE_NAME)
        val narshaRoom = RoomEntity(id = 20L, name = "나르샤실")
        stubBase(narshaType)
        whenever(studentActivityRoomRepository.findAllByUserInAndDayOfWeekOrCommon(listOf(student), DayOfWeek.MONDAY))
            .thenReturn(listOf(StudentActivityRoomEntity(1L, student, null, narshaType, narshaRoom)))

        val result = service.resolveRooms(listOf(student), MONDAY, checkpoint)

        assertEquals(narshaRoom, result[student.id])
    }

    @Test
    fun `방과후 모드는 현재 요일 활동실을 사용한다`() {
        val afterSchoolType = AttendanceTypeEntity(id = 8L, name = AttendanceTypeEntity.AFTER_SCHOOL_TYPE_NAME)
        val mondayRoom = RoomEntity(id = 21L, name = "월요일 방과후실")
        stubBase(afterSchoolType)
        whenever(studentActivityRoomRepository.findAllByUserInAndDayOfWeekOrCommon(listOf(student), DayOfWeek.MONDAY))
            .thenReturn(
                listOf(
                    StudentActivityRoomEntity(
                        1L,
                        student,
                        DayOfWeek.MONDAY,
                        afterSchoolType,
                        mondayRoom
                    )
                )
            )

        val result = service.resolveRooms(listOf(student), MONDAY, checkpoint)

        assertEquals(mondayRoom, result[student.id])
    }

    @Test
    fun `교실자습 모드는 학생 학년과 반에 해당하는 교실을 사용한다`() {
        val classroomType = AttendanceTypeEntity(id = 3L, name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)
        val homeroom = RoomEntity(id = 30L, name = "1-2", grade = 1, classNumber = 2)
        stubBase(classroomType)
        whenever(studentActivityRoomRepository.findAllByUserInAndDayOfWeekOrCommon(listOf(student), DayOfWeek.MONDAY))
            .thenReturn(emptyList())
        whenever(roomRepository.findAllByGradeIsNotNullAndClassNumberIsNotNullAndIsDeletedFalse())
            .thenReturn(listOf(homeroom))

        val result = service.resolveRooms(listOf(student), MONDAY, checkpoint)

        assertEquals(homeroom, result[student.id])
    }

    @Test
    fun `활동실 배정이 없으면 기존 스케줄실을 사용한다`() {
        val narshaType = AttendanceTypeEntity(id = 7L, name = AttendanceTypeEntity.NARSHA_TYPE_NAME)
        stubBase(narshaType)
        whenever(studentActivityRoomRepository.findAllByUserInAndDayOfWeekOrCommon(listOf(student), DayOfWeek.MONDAY))
            .thenReturn(emptyList())

        val result = service.resolveRooms(listOf(student), MONDAY, checkpoint)

        assertEquals(scheduleRoom, result[student.id])
    }

    @Test
    fun `실제 출석실은 활동실보다 우선한다`() {
        val narshaType = AttendanceTypeEntity(id = 7L, name = AttendanceTypeEntity.NARSHA_TYPE_NAME)
        val narshaRoom = RoomEntity(id = 20L, name = "나르샤실")
        val attendedRoom = RoomEntity(id = 40L, name = "실제 출석실")
        stubBase(
            narshaType,
            attendances = listOf(
                AttendanceEntity(
                    id = 1L,
                    checkpoint = checkpoint,
                    type = narshaType,
                    user = student,
                    room = attendedRoom,
                    date = MONDAY
                )
            )
        )
        whenever(studentActivityRoomRepository.findAllByUserInAndDayOfWeekOrCommon(listOf(student), DayOfWeek.MONDAY))
            .thenReturn(listOf(StudentActivityRoomEntity(1L, student, null, narshaType, narshaRoom)))

        val result = service.resolveRooms(listOf(student), MONDAY, checkpoint)

        assertEquals(attendedRoom, result[student.id])
    }

    private fun stubBase(
        sortModeType: AttendanceTypeEntity,
        attendances: List<AttendanceEntity> = emptyList()
    ) {
        val sortMode = AttendanceSortModeEntity(
            id = 1L,
            date = MONDAY,
            checkpoint = checkpoint,
            grade = 1,
            type = sortModeType
        )
        val schedule = StudentScheduleEntity(
            id = 1L,
            user = student,
            dayOfWeek = DayOfWeek.MONDAY,
            checkpoint = checkpoint,
            type = scheduleType,
            room = scheduleRoom
        )
        whenever(attendanceRepository.findAllByUsersAndCheckpointIdAndDate(listOf(student), checkpoint.id!!, MONDAY))
            .thenReturn(attendances)
        whenever(studentInfoRepository.findAllByUserIn(listOf(student))).thenReturn(listOf(studentInfo))
        whenever(attendanceSortModeRepository.findAllByDateAndCheckpoint(MONDAY, checkpoint))
            .thenReturn(listOf(sortMode))
        whenever(studentScheduleRepository.findAllByUserInAndDayOfWeekIn(listOf(student), listOf(DayOfWeek.MONDAY)))
            .thenReturn(listOf(schedule))
    }

    companion object {
        private val MONDAY: LocalDate = LocalDate.of(2026, 8, 3)
    }
}
