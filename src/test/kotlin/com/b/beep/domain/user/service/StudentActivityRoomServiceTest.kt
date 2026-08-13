package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.StudentActivityRoomRequest
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.StudentActivityRoomError
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek

@ExtendWith(MockitoExtension::class)
class StudentActivityRoomServiceTest {

    @Mock
    private lateinit var studentActivityRoomRepository: StudentActivityRoomRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var roomRepository: RoomRepository

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    private lateinit var service: StudentActivityRoomService
    private lateinit var student: UserEntity
    private lateinit var room: RoomEntity

    @BeforeEach
    fun setUp() {
        service = StudentActivityRoomService(
            studentActivityRoomRepository,
            userRepository,
            roomRepository,
            attendanceTypeService
        )
        student = UserEntity(id = 1L, username = "student", name = "학생", role = UserRole.STUDENT)
        room = RoomEntity(id = 10L, name = "프로젝트실")
        whenever(userRepository.findByIdAndIsDeletedFalse(student.id!!)).thenReturn(student)
    }

    @Test
    fun `나르샤는 요일 없는 공통 활동실로 저장한다`() {
        val type = AttendanceTypeEntity(id = 7L, name = AttendanceTypeEntity.NARSHA_TYPE_NAME)
        val request = StudentActivityRoomRequest(dayOfWeek = null, typeId = type.id!!, roomId = room.id!!)
        val saved = StudentActivityRoomEntity(
            id = 1L,
            user = student,
            dayOfWeek = null,
            type = type,
            room = room
        )
        whenever(attendanceTypeService.getAttendanceTypeEntityById(type.id!!)).thenReturn(type)
        whenever(roomRepository.findByIdAndIsDeletedFalse(room.id!!)).thenReturn(room)
        whenever(studentActivityRoomRepository.saveAll(org.mockito.kotlin.any<List<StudentActivityRoomEntity>>()))
            .thenReturn(listOf(saved))

        val result = service.replaceActivityRooms(student.id!!, listOf(request))

        val captor = argumentCaptor<List<StudentActivityRoomEntity>>()
        verify(studentActivityRoomRepository).saveAll(captor.capture())
        assertEquals(null, captor.firstValue.single().dayOfWeek)
        assertEquals(null, result.single().dayOfWeek)
    }

    @Test
    fun `동일 타입과 요일이 중복되면 저장하지 않는다`() {
        val requests = listOf(
            StudentActivityRoomRequest(null, 7L, 10L),
            StudentActivityRoomRequest(null, 7L, 11L)
        )

        val exception = assertThrows<CustomException> {
            service.replaceActivityRooms(student.id!!, requests)
        }

        assertEquals(StudentActivityRoomError.DUPLICATE_ASSIGNMENT, exception.error)
    }

    @Test
    fun `방과후는 요일이 필수다`() {
        val type = AttendanceTypeEntity(id = 8L, name = AttendanceTypeEntity.AFTER_SCHOOL_TYPE_NAME)
        whenever(attendanceTypeService.getAttendanceTypeEntityById(type.id!!)).thenReturn(type)

        val exception = assertThrows<CustomException> {
            service.replaceActivityRooms(
                student.id!!,
                listOf(StudentActivityRoomRequest(null, type.id!!, room.id!!))
            )
        }

        assertEquals(StudentActivityRoomError.DAY_OF_WEEK_REQUIRED, exception.error)
    }

    @Test
    fun `나르샤와 동아리는 요일을 지정할 수 없다`() {
        val type = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        whenever(attendanceTypeService.getAttendanceTypeEntityById(type.id!!)).thenReturn(type)

        val exception = assertThrows<CustomException> {
            service.replaceActivityRooms(
                student.id!!,
                listOf(StudentActivityRoomRequest(DayOfWeek.MONDAY, type.id!!, room.id!!))
            )
        }

        assertEquals(StudentActivityRoomError.DAY_OF_WEEK_NOT_ALLOWED, exception.error)
    }

    @Test
    fun `POTC는 활동실 타입으로 저장할 수 없다`() {
        val type = AttendanceTypeEntity(id = 9L, name = "POTC")
        whenever(attendanceTypeService.getAttendanceTypeEntityById(type.id!!)).thenReturn(type)

        val exception = assertThrows<CustomException> {
            service.replaceActivityRooms(
                student.id!!,
                listOf(StudentActivityRoomRequest(null, type.id!!, room.id!!))
            )
        }

        assertEquals(StudentActivityRoomError.UNSUPPORTED_TYPE, exception.error)
    }
}
