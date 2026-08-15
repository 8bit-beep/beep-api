package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class RoomCheckpointResolverTest {

    @Mock
    private lateinit var checkpointResolver: CheckpointResolver

    @Mock
    private lateinit var attendanceQueryRepository: AttendanceQueryRepository

    @Mock
    private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository

    @InjectMocks
    private lateinit var roomCheckpointResolver: RoomCheckpointResolver

    private lateinit var generalCheckpoint: AttendanceCheckpointEntity
    private lateinit var firstGradeCheckpoint: AttendanceCheckpointEntity
    private lateinit var activityType: AttendanceTypeEntity

    @BeforeEach
    fun setUp() {
        generalCheckpoint = checkpoint(1L, "8~9교시")
        firstGradeCheckpoint = checkpoint(2L, "9교시", DayOfWeek.MONDAY, 1)
        activityType = AttendanceTypeEntity(id = 8L, name = "방과후")
    }

    @Test
    fun `스케줄이 없는 공용실도 강제 정렬 활동실이면 1학년 체크포인트를 사용한다`() {
        val room = RoomEntity(id = 10L, name = "프로젝트실", grade = null)
        val sortMode = AttendanceSortModeEntity(
            id = 1L,
            date = MONDAY,
            checkpoint = firstGradeCheckpoint,
            grade = 1,
            type = activityType
        )
        whenever(checkpointResolver.getCurrentCheckpointOrNearest()).thenReturn(generalCheckpoint)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByGradeAndCheckpoint(
                1,
                DayOfWeek.MONDAY,
                firstGradeCheckpoint
            )
        ).thenReturn(emptySet())
        whenever(
            attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
                MONDAY,
                firstGradeCheckpoint,
                1
            )
        ).thenReturn(sortMode)
        whenever(
            attendanceQueryRepository.findActivityRoomIdsByGradeDayOrCommonAndType(
                eq(1),
                eq(DayOfWeek.MONDAY),
                eq(activityType)
            )
        ).thenReturn(setOf(room.id!!))

        val result = roomCheckpointResolver.getCurrentCheckpointOrNearest(MONDAY, room, null)

        assertEquals(firstGradeCheckpoint, result)
    }

    @Test
    fun `활동실 배정이 없는 공용실은 일반 체크포인트를 사용한다`() {
        val room = RoomEntity(id = 10L, name = "프로젝트실", grade = null)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest()).thenReturn(generalCheckpoint)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByGradeAndCheckpoint(
                1,
                DayOfWeek.MONDAY,
                firstGradeCheckpoint
            )
        ).thenReturn(emptySet())
        whenever(
            attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
                MONDAY,
                firstGradeCheckpoint,
                1
            )
        ).thenReturn(null)

        val result = roomCheckpointResolver.getCurrentCheckpointOrNearest(MONDAY, room, null)

        assertEquals(generalCheckpoint, result)
    }

    @Test
    fun `강제 정렬 활동실의 현재 체크포인트는 실 승인에도 사용한다`() {
        val room = RoomEntity(id = 10L, name = "프로젝트실", grade = null)
        val sortMode = AttendanceSortModeEntity(
            id = 1L,
            date = MONDAY,
            checkpoint = firstGradeCheckpoint,
            grade = 1,
            type = activityType
        )
        whenever(checkpointResolver.getCurrentCheckpointOrNull()).thenReturn(generalCheckpoint)
        whenever(checkpointResolver.getCurrentCheckpointOrNull(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByGradeAndCheckpoint(
                1,
                DayOfWeek.MONDAY,
                firstGradeCheckpoint
            )
        ).thenReturn(emptySet())
        whenever(
            attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
                MONDAY,
                firstGradeCheckpoint,
                1
            )
        ).thenReturn(sortMode)
        whenever(
            attendanceQueryRepository.findActivityRoomIdsByGradeDayOrCommonAndType(
                1,
                DayOfWeek.MONDAY,
                activityType
            )
        ).thenReturn(setOf(room.id!!))

        val result = roomCheckpointResolver.getCurrentCheckpoints(MONDAY, listOf(room))

        assertEquals(firstGradeCheckpoint, result[room.id])
    }

    private fun checkpoint(
        id: Long,
        name: String,
        dayOfWeek: DayOfWeek? = null,
        grade: Int? = null
    ): AttendanceCheckpointEntity {
        return AttendanceCheckpointEntity(
            id = id,
            name = name,
            startAt = LocalTime.of(16, 30),
            endAt = LocalTime.of(18, 59),
            attendanceStartAt = LocalTime.of(16, 30),
            attendanceEndAt = LocalTime.of(16, 50),
            dayOfWeek = dayOfWeek,
            grade = grade
        )
    }

    companion object {
        private val MONDAY: LocalDate = LocalDate.of(2026, 8, 3)
    }
}
