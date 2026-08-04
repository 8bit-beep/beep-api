package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceTypeError
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.CreateMyScheduleRequest
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.domain.StudentScheduleValidator
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentScheduleRepository
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
import org.mockito.kotlin.*
import java.time.DayOfWeek
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class StudentScheduleServiceTest {

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @Mock
    private lateinit var studentScheduleValidator: StudentScheduleValidator

    @Mock
    private lateinit var contextHolder: ContextHolder

    @Mock
    private lateinit var roomRepository: RoomRepository

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var studentScheduleService: StudentScheduleService

    private fun createUserEntity(
        id: Long = 1L,
        username: String = "테스트유저",
        role: UserRole = UserRole.STUDENT
    ) = UserEntity(id = id, username = username, name = username, role = role)

    private fun createRoomEntity(
        id: Long = 1L,
        name: String = "Room A"
    ) = RoomEntity(id = id, name = name)

    private fun createCheckpointEntity(
        id: Long = 1L,
        name: String = "1교시",
        startAt: LocalTime = LocalTime.of(9, 0),
        endAt: LocalTime = LocalTime.of(12, 0)
    ) = AttendanceCheckpointEntity(
        id = id,
        name = name,
        startAt = startAt,
        endAt = endAt,
        attendanceStartAt = startAt,
        attendanceEndAt = startAt.plusMinutes(20)
    )

    private fun createTypeEntity(
        id: Long = 1L,
        name: String = "출석"
    ) = AttendanceTypeEntity(id = id, name = name)

    private fun createScheduleEntity(
        id: Long = 1L,
        user: UserEntity = createUserEntity(),
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        checkpoint: AttendanceCheckpointEntity = createCheckpointEntity(),
        type: AttendanceTypeEntity = createTypeEntity(),
        room: RoomEntity = createRoomEntity()
    ) = StudentScheduleEntity(
        id = id,
        user = user,
        dayOfWeek = dayOfWeek,
        checkpoint = checkpoint,
        type = type,
        room = room
    )

    @Nested
    @DisplayName("createSchedule")
    inner class CreateSchedule {

        @Test
        @DisplayName("성공")
        fun success() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val checkpoint = createCheckpointEntity()
            val type = createTypeEntity()
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(1L)).thenReturn(type)
            `when`(studentScheduleRepository.save(any<StudentScheduleEntity>())).thenAnswer { it.arguments[0] }

            studentScheduleService.createSchedule(request)

            verify(studentScheduleValidator).validateDayOfWeek(DayOfWeek.MONDAY)
            verify(studentScheduleValidator).validateNotDuplicate(user, DayOfWeek.MONDAY, checkpoint)
            verify(studentScheduleRepository).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("유저 없으면 예외")
        fun userNotFound_throwsException() {
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createSchedule(request)
            }

            assertEquals(UserError.USER_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("실 없으면 예외")
        fun roomNotFound_throwsException() {
            val user = createUserEntity()
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createSchedule(request)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("체크포인트 없으면 예외")
        fun checkpointNotFound_throwsException() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createSchedule(request)
            }

            assertEquals(CheckpointError.CHECKPOINT_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("타입 없으면 예외")
        fun typeNotFound_throwsException() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val checkpoint = createCheckpointEntity()
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(1L))
                .thenThrow(CustomException(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND))

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createSchedule(request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("중복 스케줄 시 예외")
        fun duplicateSchedule_throwsException() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val checkpoint = createCheckpointEntity()
            val type = createTypeEntity()
            val request = CreateStudentScheduleRequest(
                userId = 1L,
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(1L)).thenReturn(type)
            doThrow(CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE))
                .`when`(studentScheduleValidator).validateNotDuplicate(user, DayOfWeek.MONDAY, checkpoint)

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createSchedule(request)
            }

            assertEquals(StudentScheduleError.ALREADY_EXIST_SCHEDULE, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }
    }

    @Nested
    @DisplayName("createMySchedule")
    inner class CreateMySchedule {

        @Test
        @DisplayName("성공")
        fun success() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val checkpoint = createCheckpointEntity()
            val type = createTypeEntity()
            val request = CreateMyScheduleRequest(
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(contextHolder.user).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(1L)).thenReturn(type)
            `when`(studentScheduleRepository.save(any<StudentScheduleEntity>())).thenAnswer { it.arguments[0] }

            studentScheduleService.createMySchedule(request)

            verify(studentScheduleValidator).validateDayOfWeek(DayOfWeek.MONDAY)
            verify(studentScheduleValidator).validateNotDuplicate(user, DayOfWeek.MONDAY, checkpoint)
            verify(studentScheduleRepository).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("실 없으면 예외")
        fun roomNotFound_throwsException() {
            val user = createUserEntity()
            val request = CreateMyScheduleRequest(
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(contextHolder.user).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createMySchedule(request)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("체크포인트 없으면 예외")
        fun checkpointNotFound_throwsException() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val request = CreateMyScheduleRequest(
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(contextHolder.user).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createMySchedule(request)
            }

            assertEquals(CheckpointError.CHECKPOINT_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("중복 스케줄 시 예외")
        fun duplicateSchedule_throwsException() {
            val user = createUserEntity()
            val room = createRoomEntity()
            val checkpoint = createCheckpointEntity()
            val type = createTypeEntity()
            val request = CreateMyScheduleRequest(
                dayOfWeek = DayOfWeek.MONDAY,
                checkpointId = 1L,
                typeId = 1L,
                roomId = 1L
            )

            `when`(contextHolder.user).thenReturn(user)
            `when`(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room))
            `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(1L)).thenReturn(type)
            doThrow(CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE))
                .`when`(studentScheduleValidator).validateNotDuplicate(user, DayOfWeek.MONDAY, checkpoint)

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.createMySchedule(request)
            }

            assertEquals(StudentScheduleError.ALREADY_EXIST_SCHEDULE, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }
    }

    @Nested
    @DisplayName("updateSchedule")
    inner class UpdateSchedule {

        @Test
        @DisplayName("성공 - 전체 업데이트")
        fun success_fullUpdate() {
            val user = createUserEntity()
            val schedule = createScheduleEntity(user = user)
            val newRoom = createRoomEntity(id = 2L, name = "Room B")
            val newCheckpoint = createCheckpointEntity(id = 2L, name = "2교시")
            val newType = createTypeEntity(id = 2L, name = "외출")
            val request = UpdateStudentScheduleRequest(
                dayOfWeek = DayOfWeek.TUESDAY,
                checkpointId = 2L,
                typeId = 2L,
                roomId = 2L
            )

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule))
            `when`(checkpointRepository.findById(2L)).thenReturn(java.util.Optional.of(newCheckpoint))
            `when`(attendanceTypeService.getAttendanceTypeEntityById(2L)).thenReturn(newType)
            `when`(roomRepository.findById(2L)).thenReturn(java.util.Optional.of(newRoom))
            `when`(studentScheduleRepository.save(any<StudentScheduleEntity>())).thenAnswer { it.arguments[0] }

            studentScheduleService.updateSchedule(1L, request)

            verify(studentScheduleValidator).validateDayOfWeek(DayOfWeek.TUESDAY)
            verify(studentScheduleValidator).validateNotDuplicateExcluding(user, DayOfWeek.TUESDAY, newCheckpoint, 1L)
            verify(studentScheduleRepository).save(schedule)
            assertEquals(DayOfWeek.TUESDAY, schedule.dayOfWeek)
        }

        @Test
        @DisplayName("성공 - 부분 업데이트")
        fun success_partialUpdate() {
            val schedule = createScheduleEntity()
            val request = UpdateStudentScheduleRequest(
                dayOfWeek = null,
                checkpointId = null,
                typeId = null,
                roomId = null
            )

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule))
            `when`(studentScheduleRepository.save(any<StudentScheduleEntity>())).thenAnswer { it.arguments[0] }

            studentScheduleService.updateSchedule(1L, request)

            verify(studentScheduleValidator, never()).validateDayOfWeek(any())
            verify(studentScheduleRepository).save(schedule)
            assertEquals(DayOfWeek.MONDAY, schedule.dayOfWeek)
        }

        @Test
        @DisplayName("스케줄 없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateStudentScheduleRequest(dayOfWeek = DayOfWeek.TUESDAY)

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.updateSchedule(1L, request)
            }

            assertEquals(StudentScheduleError.SCHEDULE_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).save(any<StudentScheduleEntity>())
        }

        @Test
        @DisplayName("실 없으면 예외")
        fun roomNotFound_throwsException() {
            val schedule = createScheduleEntity()
            val request = UpdateStudentScheduleRequest(roomId = 999L)

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule))
            `when`(roomRepository.findById(999L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.updateSchedule(1L, request)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }

        @Test
        @DisplayName("중복 스케줄 시 예외")
        fun duplicateSchedule_throwsException() {
            val user = createUserEntity()
            val schedule = createScheduleEntity(user = user)
            val newCheckpoint = createCheckpointEntity(id = 2L, name = "2교시")
            val request = UpdateStudentScheduleRequest(
                dayOfWeek = DayOfWeek.TUESDAY,
                checkpointId = 2L
            )

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule))
            `when`(checkpointRepository.findById(2L)).thenReturn(java.util.Optional.of(newCheckpoint))
            doThrow(CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE))
                .`when`(studentScheduleValidator).validateNotDuplicateExcluding(user, DayOfWeek.TUESDAY, newCheckpoint, 1L)

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.updateSchedule(1L, request)
            }

            assertEquals(StudentScheduleError.ALREADY_EXIST_SCHEDULE, exception.error)
        }
    }

    @Nested
    @DisplayName("deleteSchedule")
    inner class DeleteSchedule {

        @Test
        @DisplayName("성공")
        fun success() {
            val schedule = createScheduleEntity()

            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule))

            studentScheduleService.deleteSchedule(1L)

            verify(studentScheduleRepository).delete(schedule)
        }

        @Test
        @DisplayName("스케줄 없으면 예외")
        fun notFound_throwsException() {
            `when`(studentScheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.deleteSchedule(1L)
            }

            assertEquals(StudentScheduleError.SCHEDULE_NOT_FOUND, exception.error)
            verify(studentScheduleRepository, never()).delete(any<StudentScheduleEntity>())
        }
    }

    @Nested
    @DisplayName("getMySchedules")
    inner class GetMySchedules {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val user = createUserEntity()
            val schedules = listOf(
                createScheduleEntity(id = 1L, user = user),
                createScheduleEntity(id = 2L, user = user, dayOfWeek = DayOfWeek.TUESDAY)
            )

            `when`(contextHolder.user).thenReturn(user)
            `when`(studentScheduleRepository.findAllByUserOrderByDayOfWeekAscCheckpointStartAtAsc(user)).thenReturn(schedules)

            val result = studentScheduleService.getMySchedules()

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            val user = createUserEntity()

            `when`(contextHolder.user).thenReturn(user)
            `when`(studentScheduleRepository.findAllByUserOrderByDayOfWeekAscCheckpointStartAtAsc(user)).thenReturn(emptyList<StudentScheduleEntity>())

            val result = studentScheduleService.getMySchedules()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getSchedulesByUserId")
    inner class GetSchedulesByUserId {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val user = createUserEntity()
            val schedules = listOf(
                createScheduleEntity(id = 1L, user = user),
                createScheduleEntity(id = 2L, user = user, dayOfWeek = DayOfWeek.TUESDAY)
            )

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(studentScheduleRepository.findAllByUserOrderByDayOfWeekAscCheckpointStartAtAsc(user)).thenReturn(schedules)

            val result = studentScheduleService.getSchedulesByUserId(1L)

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            val user = createUserEntity()

            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(user)
            `when`(studentScheduleRepository.findAllByUserOrderByDayOfWeekAscCheckpointStartAtAsc(user)).thenReturn(emptyList<StudentScheduleEntity>())

            val result = studentScheduleService.getSchedulesByUserId(1L)

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("유저 없으면 예외")
        fun userNotFound_throwsException() {
            `when`(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                studentScheduleService.getSchedulesByUserId(1L)
            }

            assertEquals(UserError.USER_NOT_FOUND, exception.error)
        }
    }
}
