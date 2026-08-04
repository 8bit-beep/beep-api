package com.b.beep.domain.room.service

import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomApprovalError
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class RoomApprovalServiceTest {

    @Mock
    private lateinit var roomApprovalRepository: RoomApprovalRepository

    @Mock
    private lateinit var roomRepository: RoomRepository

    @Mock
    private lateinit var contextHolder: ContextHolder

    @Mock
    private lateinit var roomCheckpointResolver: RoomCheckpointResolver

    @InjectMocks
    private lateinit var roomApprovalService: RoomApprovalService

    private lateinit var checkpoint: AttendanceCheckpointEntity
    private lateinit var room: RoomEntity
    private lateinit var teacher: UserEntity

    @BeforeEach
    fun setUp() {
        checkpoint = AttendanceCheckpointEntity(
            id = 1L,
            name = "1교시",
            startAt = LocalTime.of(9, 0),
            endAt = LocalTime.of(12, 0),
            attendanceStartAt = LocalTime.of(9, 0),
            attendanceEndAt = LocalTime.of(9, 20)
        )
        room = RoomEntity(id = 1L, name = "Room A", grade = 1, classNumber = 1, floor = 1)
        teacher = UserEntity(id = 1L, username = "Teacher", name = "Teacher", role = UserRole.TEACHER)
    }

    @Nested
    @DisplayName("createApproval")
    inner class CreateApproval {

        @Test
        @DisplayName("성공")
        fun success() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.existsByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(false)
            `when`(contextHolder.user).thenReturn(teacher)
            `when`(roomApprovalRepository.save(any<RoomApprovalEntity>())).thenAnswer { it.arguments[0] }

            roomApprovalService.createApproval(1L)

            verify(roomApprovalRepository).save(any<RoomApprovalEntity>())
        }

        @Test
        @DisplayName("방 없음 시 예외")
        fun roomNotFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.createApproval(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
            verify(roomApprovalRepository, never()).save(any<RoomApprovalEntity>())
        }

        @Test
        @DisplayName("이미 승인됨 시 예외")
        fun alreadyApproved_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.existsByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.createApproval(1L)
            }

            assertEquals(RoomApprovalError.ALREADY_APPROVED, exception.error)
            verify(roomApprovalRepository, never()).save(any<RoomApprovalEntity>())
        }

        @Test
        @DisplayName("동시성 예외 처리")
        fun concurrentRequest_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.existsByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(false)
            `when`(contextHolder.user).thenReturn(teacher)
            `when`(roomApprovalRepository.save(any<RoomApprovalEntity>()))
                .thenThrow(DataIntegrityViolationException("Duplicate"))

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.createApproval(1L)
            }

            assertEquals(RoomApprovalError.ALREADY_APPROVED, exception.error)
        }
    }

    @Nested
    @DisplayName("getApprovals")
    inner class GetApprovals {

        @Test
        @DisplayName("전체 조회")
        fun getAllApprovals() {
            val room2 = RoomEntity(id = 2L, name = "Room B", grade = 1, classNumber = 2, floor = 1)
            val approval = RoomApprovalEntity(
                id = 1L, checkpoint = checkpoint, room = room, teacher = teacher, date = LocalDate.now()
            )

            `when`(roomRepository.findAllByIsDeletedFalseOrderByFloorAscNameAsc()).thenReturn(listOf(room, room2))
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room, room2))))
                .thenReturn(mapOf(room.id!! to checkpoint, room2.id!! to checkpoint))
            `when`(roomApprovalRepository.findAllByDate(any())).thenReturn(listOf(approval))

            val result = roomApprovalService.getApprovals(null)

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("승인만 조회")
        fun getApprovedOnly() {
            val room2 = RoomEntity(id = 2L, name = "Room B", grade = 1, classNumber = 2, floor = 1)
            val approval = RoomApprovalEntity(
                id = 1L, checkpoint = checkpoint, room = room, teacher = teacher, date = LocalDate.now()
            )

            `when`(roomRepository.findAllByIsDeletedFalseOrderByFloorAscNameAsc()).thenReturn(listOf(room, room2))
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room, room2))))
                .thenReturn(mapOf(room.id!! to checkpoint, room2.id!! to checkpoint))
            `when`(roomApprovalRepository.findAllByDate(any())).thenReturn(listOf(approval))

            val result = roomApprovalService.getApprovals(true)

            assertEquals(1, result.size)
            assertTrue(result[0].approved)
        }

        @Test
        @DisplayName("미승인만 조회")
        fun getUnapprovedOnly() {
            val room2 = RoomEntity(id = 2L, name = "Room B", grade = 1, classNumber = 2, floor = 1)
            val approval = RoomApprovalEntity(
                id = 1L, checkpoint = checkpoint, room = room, teacher = teacher, date = LocalDate.now()
            )

            `when`(roomRepository.findAllByIsDeletedFalseOrderByFloorAscNameAsc()).thenReturn(listOf(room, room2))
            `when`(roomCheckpointResolver.getCurrentCheckpoints(any(), eq(listOf(room, room2))))
                .thenReturn(mapOf(room.id!! to checkpoint, room2.id!! to checkpoint))
            `when`(roomApprovalRepository.findAllByDate(any())).thenReturn(listOf(approval))

            val result = roomApprovalService.getApprovals(false)

            assertEquals(1, result.size)
            assertFalse(result[0].approved)
        }
    }

    @Nested
    @DisplayName("getApproval")
    inner class GetApproval {

        @Test
        @DisplayName("승인됨")
        fun approved() {
            val approval = RoomApprovalEntity(
                id = 1L, checkpoint = checkpoint, room = room, teacher = teacher, date = LocalDate.now()
            )

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.findByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(approval)

            val result = roomApprovalService.getApproval(1L)

            assertTrue(result.approved)
            assertNotNull(result.approvedTeacher)
        }

        @Test
        @DisplayName("미승인")
        fun notApproved() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.findByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(null)

            val result = roomApprovalService.getApproval(1L)

            assertFalse(result.approved)
            assertNull(result.approvedTeacher)
        }

        @Test
        @DisplayName("방 없음 시 예외")
        fun roomNotFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.getApproval(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("deleteApproval")
    inner class DeleteApproval {

        @Test
        @DisplayName("성공")
        fun success() {
            val approval = RoomApprovalEntity(
                id = 1L, checkpoint = checkpoint, room = room, teacher = teacher, date = LocalDate.now()
            )

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.findByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(approval)

            roomApprovalService.deleteApproval(1L)

            verify(roomApprovalRepository).delete(approval)
        }

        @Test
        @DisplayName("방 없음 시 예외")
        fun roomNotFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.deleteApproval(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
            verify(roomApprovalRepository, never()).delete(any<RoomApprovalEntity>())
        }

        @Test
        @DisplayName("승인 없음 시 예외")
        fun approvalNotFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomCheckpointResolver.getCurrentCheckpoint(any(), eq(room))).thenReturn(checkpoint)
            `when`(roomApprovalRepository.findByCheckpointAndRoomAndDate(eq(checkpoint), eq(room), any()))
                .thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                roomApprovalService.deleteApproval(1L)
            }

            assertEquals(RoomApprovalError.APPROVAL_NOT_FOUND, exception.error)
            verify(roomApprovalRepository, never()).delete(any<RoomApprovalEntity>())
        }
    }
}
