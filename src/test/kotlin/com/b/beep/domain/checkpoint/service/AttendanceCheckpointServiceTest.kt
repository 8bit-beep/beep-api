package com.b.beep.domain.checkpoint.service

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.shift.repository.ShiftRepository
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
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AttendanceCheckpointServiceTest {

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var shiftRepository: ShiftRepository

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @InjectMocks
    private lateinit var checkpointService: AttendanceCheckpointService

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

    @Nested
    @DisplayName("createCheckPoint")
    inner class CreateCheckPoint {

        @Test
        @DisplayName("성공")
        fun success() {
            val request = CreateCheckpointRequest(
                name = "1교시",
                startAt = LocalTime.of(9, 0),
                endAt = LocalTime.of(12, 0),
                attendanceStartAt = LocalTime.of(9, 0),
                attendanceEndAt = LocalTime.of(9, 20)
            )

            `when`(checkpointRepository.findAll()).thenReturn(emptyList<AttendanceCheckpointEntity>())
            `when`(checkpointRepository.save(any<AttendanceCheckpointEntity>())).thenAnswer { it.arguments[0] }

            checkpointService.createCheckPoint(request)

            verify(checkpointRepository).save(any<AttendanceCheckpointEntity>())
        }

        @Test
        @DisplayName("시간 겹침 시 예외 - 시작 시간 겹침")
        fun timeOverlap_startOverlaps() {
            val existing = createCheckpointEntity(startAt = LocalTime.of(9, 0), endAt = LocalTime.of(12, 0))
            val request = CreateCheckpointRequest(
                name = "2교시",
                startAt = LocalTime.of(11, 0),
                endAt = LocalTime.of(14, 0),
                attendanceStartAt = LocalTime.of(11, 0),
                attendanceEndAt = LocalTime.of(11, 20)
            )

            `when`(checkpointRepository.findAll()).thenReturn(listOf(existing))

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.createCheckPoint(request)
            }

            assertEquals(CheckpointError.CHECKPOINT_TIME_OVERLAP, exception.error)
            verify(checkpointRepository, never()).save(any<AttendanceCheckpointEntity>())
        }

        @Test
        @DisplayName("시간 겹침 시 예외 - 종료 시간 겹침")
        fun timeOverlap_endOverlaps() {
            val existing = createCheckpointEntity(startAt = LocalTime.of(12, 0), endAt = LocalTime.of(15, 0))
            val request = CreateCheckpointRequest(
                name = "1교시",
                startAt = LocalTime.of(9, 0),
                endAt = LocalTime.of(13, 0),
                attendanceStartAt = LocalTime.of(9, 0),
                attendanceEndAt = LocalTime.of(9, 20)
            )

            `when`(checkpointRepository.findAll()).thenReturn(listOf(existing))

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.createCheckPoint(request)
            }

            assertEquals(CheckpointError.CHECKPOINT_TIME_OVERLAP, exception.error)
        }

        @Test
        @DisplayName("시간 겹치지 않음")
        fun noOverlap() {
            val existing = createCheckpointEntity(startAt = LocalTime.of(9, 0), endAt = LocalTime.of(12, 0))
            val request = CreateCheckpointRequest(
                name = "2교시",
                startAt = LocalTime.of(13, 0),
                endAt = LocalTime.of(16, 0),
                attendanceStartAt = LocalTime.of(13, 0),
                attendanceEndAt = LocalTime.of(13, 20)
            )

            `when`(checkpointRepository.findAll()).thenReturn(listOf(existing))
            `when`(checkpointRepository.save(any<AttendanceCheckpointEntity>())).thenAnswer { it.arguments[0] }

            checkpointService.createCheckPoint(request)

            verify(checkpointRepository).save(any<AttendanceCheckpointEntity>())
        }
    }

    @Nested
    @DisplayName("getCheckPoints")
    inner class GetCheckPoints {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val checkpoints = listOf(
                createCheckpointEntity(id = 1L, name = "1교시"),
                createCheckpointEntity(id = 2L, name = "2교시")
            )

            `when`(checkpointRepository.findAll()).thenReturn(checkpoints)

            val result = checkpointService.getCheckPoints()

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            `when`(checkpointRepository.findAll()).thenReturn(emptyList<AttendanceCheckpointEntity>())

            val result = checkpointService.getCheckPoints()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getCheckPoint")
    inner class GetCheckPoint {

        @Test
        @DisplayName("성공")
        fun success() {
            val checkpoint = createCheckpointEntity()

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))

            val result = checkpointService.getCheckPoint(1L)

            assertEquals("1교시", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.getCheckPoint(1L)
            }

            assertEquals(CheckpointError.CHECKPOINT_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("updateCheckPoint")
    inner class UpdateCheckPoint {

        @Test
        @DisplayName("성공 - 전체 업데이트")
        fun success_fullUpdate() {
            val checkpoint = createCheckpointEntity()
            val request = UpdateCheckpointRequest(
                name = "수정된 교시",
                startAt = LocalTime.of(10, 0),
                endAt = LocalTime.of(13, 0),
                attendanceStartAt = LocalTime.of(10, 0),
                attendanceEndAt = LocalTime.of(10, 20)
            )

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(checkpointRepository.findAll()).thenReturn(listOf(checkpoint))
            `when`(checkpointRepository.save(any<AttendanceCheckpointEntity>())).thenAnswer { it.arguments[0] }

            checkpointService.updateCheckPoint(1L, request)

            assertEquals("수정된 교시", checkpoint.name)
            assertEquals(LocalTime.of(10, 0), checkpoint.startAt)
            verify(checkpointRepository).save(checkpoint)
        }

        @Test
        @DisplayName("성공 - 부분 업데이트")
        fun success_partialUpdate() {
            val checkpoint = createCheckpointEntity()
            val request = UpdateCheckpointRequest(
                name = "수정된 교시",
                startAt = null,
                endAt = null,
                attendanceStartAt = null,
                attendanceEndAt = null
            )

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(checkpointRepository.findAll()).thenReturn(listOf(checkpoint))
            `when`(checkpointRepository.save(any<AttendanceCheckpointEntity>())).thenAnswer { it.arguments[0] }

            checkpointService.updateCheckPoint(1L, request)

            assertEquals("수정된 교시", checkpoint.name)
            assertEquals(LocalTime.of(9, 0), checkpoint.startAt)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateCheckpointRequest(name = "수정", startAt = null, endAt = null, attendanceStartAt = null, attendanceEndAt = null)

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.updateCheckPoint(1L, request)
            }

            assertEquals(CheckpointError.CHECKPOINT_NOT_FOUND, exception.error)
        }

        @Test
        @DisplayName("시간 겹침 시 예외")
        fun timeOverlap_throwsException() {
            val checkpoint = createCheckpointEntity(id = 1L, startAt = LocalTime.of(9, 0), endAt = LocalTime.of(12, 0))
            val other = createCheckpointEntity(id = 2L, startAt = LocalTime.of(13, 0), endAt = LocalTime.of(16, 0))
            val request = UpdateCheckpointRequest(
                name = null,
                startAt = LocalTime.of(14, 0),
                endAt = LocalTime.of(17, 0),
                attendanceStartAt = null,
                attendanceEndAt = null
            )

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(checkpointRepository.findAll()).thenReturn(listOf(checkpoint, other))

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.updateCheckPoint(1L, request)
            }

            assertEquals(CheckpointError.CHECKPOINT_TIME_OVERLAP, exception.error)
        }
    }

    @Nested
    @DisplayName("deleteCheckPoint")
    inner class DeleteCheckPoint {

        @Test
        @DisplayName("성공")
        fun success() {
            val checkpoint = createCheckpointEntity()

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(attendanceRepository.existsByCheckpoint(checkpoint)).thenReturn(false)
            `when`(shiftRepository.existsByCheckpoint(checkpoint)).thenReturn(false)
            `when`(studentScheduleRepository.existsByCheckpoint(checkpoint)).thenReturn(false)

            checkpointService.deleteCheckPoint(1L)

            verify(checkpointRepository).delete(checkpoint)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.deleteCheckPoint(1L)
            }

            assertEquals(CheckpointError.CHECKPOINT_NOT_FOUND, exception.error)
        }

        @Test
        @DisplayName("출석에서 사용 중 시 예외")
        fun inUseByAttendance_throwsException() {
            val checkpoint = createCheckpointEntity()

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(attendanceRepository.existsByCheckpoint(checkpoint)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.deleteCheckPoint(1L)
            }

            assertEquals(CheckpointError.CHECKPOINT_IN_USE, exception.error)
            verify(checkpointRepository, never()).delete(any<AttendanceCheckpointEntity>())
        }

        @Test
        @DisplayName("이석에서 사용 중 시 예외")
        fun inUseByShift_throwsException() {
            val checkpoint = createCheckpointEntity()

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(attendanceRepository.existsByCheckpoint(checkpoint)).thenReturn(false)
            `when`(shiftRepository.existsByCheckpoint(checkpoint)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.deleteCheckPoint(1L)
            }

            assertEquals(CheckpointError.CHECKPOINT_IN_USE, exception.error)
            verify(checkpointRepository, never()).delete(any<AttendanceCheckpointEntity>())
        }

        @Test
        @DisplayName("스케줄에서 사용 중 시 예외")
        fun inUseBySchedule_throwsException() {
            val checkpoint = createCheckpointEntity()

            `when`(checkpointRepository.findById(1L)).thenReturn(Optional.of(checkpoint))
            `when`(attendanceRepository.existsByCheckpoint(checkpoint)).thenReturn(false)
            `when`(shiftRepository.existsByCheckpoint(checkpoint)).thenReturn(false)
            `when`(studentScheduleRepository.existsByCheckpoint(checkpoint)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                checkpointService.deleteCheckPoint(1L)
            }

            assertEquals(CheckpointError.CHECKPOINT_IN_USE, exception.error)
            verify(checkpointRepository, never()).delete(any<AttendanceCheckpointEntity>())
        }
    }
}
