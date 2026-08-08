package com.b.beep.domain.room.service

import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.domain.RoomClubNameResolver
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class RoomServiceTest {

    @Mock
    private lateinit var roomRepository: RoomRepository

    @Mock
    private lateinit var roomCheckpointResolver: RoomCheckpointResolver

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var roomClubNameResolver: RoomClubNameResolver

    @InjectMocks
    private lateinit var roomService: RoomService

    private fun createCheckpoint(id: Long = 1L) = AttendanceCheckpointEntity(
        id = id,
        name = "1교시",
        startAt = LocalTime.of(9, 0),
        endAt = LocalTime.of(12, 0),
        attendanceStartAt = LocalTime.of(9, 0),
        attendanceEndAt = LocalTime.of(9, 20)
    )

    private fun createStudent(id: Long) = UserEntity(id = id, username = "student$id", name = "Student$id", role = UserRole.STUDENT)

    private fun createRoomEntity(
        id: Long = 1L,
        name: String = "Room A",
        grade: Int? = 1,
        classNumber: Int? = 1,
        floor: Int? = 1,
        clubName: String? = null
    ) = RoomEntity(
        id = id,
        name = name,
        grade = grade,
        classNumber = classNumber,
        floor = floor,
        clubName = clubName
    )


    @Nested
    @DisplayName("createRoom")
    inner class CreateRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val request = CreateRoomRequest(name = "Room A", grade = 1, classNumber = 1, floor = 1)
            val savedRoom = createRoomEntity()

            `when`(roomRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(false)
            `when`(roomRepository.save(any<RoomEntity>())).thenReturn(savedRoom)

            val result = roomService.createRoom(request)

            assertEquals("Room A", result.name)
            verify(roomRepository).existsByNameAndIsDeletedFalse(request.name)
            verify(roomRepository).save(any<RoomEntity>())
        }

        @Test
        @DisplayName("동아리명은 실제 이름과 함께 그대로 저장 및 반환된다")
        fun clubName() {
            val request = CreateRoomRequest(name = "Room A", grade = 1, classNumber = 1, floor = 1, clubName = "밴드부")
            val savedRoom = createRoomEntity(clubName = "밴드부")

            `when`(roomRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(false)
            `when`(roomRepository.save(any<RoomEntity>())).thenReturn(savedRoom)

            val result = roomService.createRoom(request)

            assertEquals("Room A", result.name)
            assertEquals("밴드부", result.clubName)
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val request = CreateRoomRequest(name = "Room A", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                roomService.createRoom(request)
            }

            assertEquals(RoomError.ROOM_ALREADY_EXISTS, exception.error)
            verify(roomRepository, never()).save(any<RoomEntity>())
        }
    }

    @Nested
    @DisplayName("getRooms")
    inner class GetRooms {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val rooms = listOf(
                createRoomEntity(id = 1L, name = "Room A"),
                createRoomEntity(id = 2L, name = "Room B")
            )

            `when`(roomRepository.findAllByIsDeletedFalse()).thenReturn(rooms)
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(rooms))).thenReturn(emptyMap())
            `when`(roomClubNameResolver.resolveDisplayNames(any(), any()))
                .thenReturn(rooms.associate { it.id!! to it.name })

            val result = roomService.getRooms()

            assertEquals(2, result.size)
            assertEquals("Room A", result[0].name)
            assertEquals("Room B", result[1].name)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            `when`(roomRepository.findAllByIsDeletedFalse()).thenReturn(emptyList<RoomEntity>())
            `when`(roomClubNameResolver.resolveDisplayNames(any(), any())).thenReturn(emptyMap<Long, String>())

            val result = roomService.getRooms()

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("동아리 시간에는 리졸버가 반환한 동아리명을 사용한다")
        fun clubName() {
            val room = createRoomEntity(id = 1L, name = "1-2", clubName = "밴드부")

            `when`(roomRepository.findAllByIsDeletedFalse()).thenReturn(listOf(room))
            `when`(roomClubNameResolver.resolveDisplayNames(any(), any())).thenReturn(mapOf(1L to "밴드부"))

            val result = roomService.getRooms()

            assertEquals("밴드부", result[0].name)
        }
    }

    @Nested
    @DisplayName("getRoom")
    inner class GetRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val room = createRoomEntity()

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomClubNameResolver.resolveDisplayNames(any(), any())).thenReturn(mapOf(1L to "Room A"))

            val result = roomService.getRoom(1L)

            assertEquals("Room A", result.name)
        }

        @Test
        @DisplayName("동아리 시간에는 리졸버가 반환한 동아리명을 사용한다")
        fun clubName() {
            val room = createRoomEntity(id = 1L, name = "1-2", clubName = "밴드부")

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomClubNameResolver.resolveDisplayNames(any(), any())).thenReturn(mapOf(1L to "밴드부"))

            val result = roomService.getRoom(1L)

            assertEquals("밴드부", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                roomService.getRoom(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }

        @Test
        @DisplayName("현재 학생 수 - 교실자습 타입만 포함하고 다른 타입은 제외")
        fun currentStudentCount_onlyClassroomStudyCounted() {
            val room = createRoomEntity()
            val checkpoint = createCheckpoint()
            val classroomStudyType = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)
            val clubType = AttendanceTypeEntity(id = 2L, name = "동아리")
            val student1 = createStudent(1L)
            val student2 = createStudent(2L)
            val schedules = listOf(
                StudentScheduleEntity(user = student1, dayOfWeek = DayOfWeek.MONDAY, checkpoint = checkpoint, type = classroomStudyType, room = room),
                StudentScheduleEntity(user = student2, dayOfWeek = DayOfWeek.MONDAY, checkpoint = checkpoint, type = classroomStudyType, room = room)
            )
            val attendances = listOf(
                AttendanceEntity(checkpoint = checkpoint, type = classroomStudyType, user = student1, room = room, date = LocalDate.now()),
                AttendanceEntity(checkpoint = checkpoint, type = clubType, user = student2, room = room, date = LocalDate.now())
            )

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room)))).thenReturn(mapOf(room.id!! to checkpoint))
            `when`(studentScheduleRepository.findAllByRoomAndDayOfWeekAndCheckpoint(eq(room), any(), eq(checkpoint))).thenReturn(schedules)
            `when`(attendanceRepository.findAllByUsersAndCheckpointIdAndDate(any(), eq(checkpoint.id!!), any())).thenReturn(attendances)

            val result = roomService.getRoom(1L)

            assertEquals(1, result.currentStudentCount)
        }

        @Test
        @DisplayName("현재 학생 수 - 출석 기록 없는 학생(미출석)은 제외")
        fun currentStudentCount_noAttendanceRecordExcluded() {
            val room = createRoomEntity()
            val checkpoint = createCheckpoint()
            val classroomStudyType = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)
            val student1 = createStudent(1L)
            val schedules = listOf(
                StudentScheduleEntity(user = student1, dayOfWeek = DayOfWeek.MONDAY, checkpoint = checkpoint, type = classroomStudyType, room = room)
            )

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room)))).thenReturn(mapOf(room.id!! to checkpoint))
            `when`(studentScheduleRepository.findAllByRoomAndDayOfWeekAndCheckpoint(eq(room), any(), eq(checkpoint))).thenReturn(schedules)
            `when`(attendanceRepository.findAllByUsersAndCheckpointIdAndDate(any(), eq(checkpoint.id!!), any())).thenReturn(emptyList())

            val result = roomService.getRoom(1L)

            assertEquals(0, result.currentStudentCount)
        }

        @Test
        @DisplayName("현재 학생 수 - 현재 체크포인트 없으면 0")
        fun currentStudentCount_noCurrentCheckpoint_zero() {
            val room = createRoomEntity()

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room)))).thenReturn(emptyMap())

            val result = roomService.getRoom(1L)

            assertEquals(0, result.currentStudentCount)
        }
    }

    @Nested
    @DisplayName("updateRoom")
    inner class UpdateRoom {

        @Test
        @DisplayName("성공 - 같은 이름")
        fun success_sameName() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room A", grade = 2, classNumber = 2, floor = 2)

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)

            val result = roomService.updateRoom(1L, request)

            assertEquals("Room A", result.name)
            assertEquals(2, result.grade)
            verify(roomRepository, never()).existsByNameAndIsDeletedFalse(any())
        }

        @Test
        @DisplayName("성공 - 새 이름")
        fun success_newName() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomRepository.existsByNameAndIsDeletedFalse("Room B")).thenReturn(false)

            val result = roomService.updateRoom(1L, request)

            assertEquals("Room B", result.name)
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)
            `when`(roomRepository.existsByNameAndIsDeletedFalse("Room B")).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                roomService.updateRoom(1L, request)
            }

            assertEquals(RoomError.ROOM_ALREADY_EXISTS, exception.error)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                roomService.updateRoom(1L, request)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("deleteRoom")
    inner class DeleteRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val room = createRoomEntity()

            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(room)

            roomService.deleteRoom(1L)

            assertTrue(room.isDeleted)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                roomService.deleteRoom(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }
    }
}
