package com.b.beep.domain.room.domain

import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class RoomClubNameResolverTest {

    @Mock
    private lateinit var roomCheckpointResolver: RoomCheckpointResolver

    @Mock
    private lateinit var attendanceQueryRepository: AttendanceQueryRepository

    @Mock
    private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    @InjectMocks
    private lateinit var resolver: RoomClubNameResolver

    private lateinit var checkpoint: AttendanceCheckpointEntity
    private lateinit var clubType: AttendanceTypeEntity

    @BeforeEach
    fun setUp() {
        checkpoint = AttendanceCheckpointEntity(
            id = 1L,
            name = "9교시",
            startAt = LocalTime.of(16, 30),
            endAt = LocalTime.of(17, 20),
            attendanceStartAt = LocalTime.of(16, 30),
            attendanceEndAt = LocalTime.of(16, 40)
        )
        clubType = AttendanceTypeEntity(id = 5L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
    }

    @Test
    fun `동아리명이 없는 실은 조회 없이 실제 이름을 그대로 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, classNumber = 2, clubName = null)

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
    }

    @Test
    fun `스케줄상 동아리 시간인 실은 동아리명을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "밴드부실", grade = null, clubName = "밴드부")
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME))
            .thenReturn(clubType)
        whenever(roomCheckpointResolver.getCurrentCheckpointOrNearest(WEDNESDAY, room, null))
            .thenReturn(checkpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByDayCheckpointAndType(
                DayOfWeek.WEDNESDAY,
                checkpoint,
                clubType
            )
        ).thenReturn(setOf(room.id!!))
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 1))
            .thenReturn(null)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 2))
            .thenReturn(null)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 3))
            .thenReturn(null)

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("밴드부", result[room.id])
    }

    @Test
    fun `강제 정렬로 동아리가 된 실은 동아리명을 반환한다`() {
        val room = RoomEntity(id = 2L, name = "프로젝트실", grade = null, clubName = "댄스부")
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME))
            .thenReturn(clubType)
        val sortMode = AttendanceSortModeEntity(
            id = 10L,
            date = WEDNESDAY,
            checkpoint = checkpoint,
            grade = 1,
            type = clubType
        )
        whenever(roomCheckpointResolver.getCurrentCheckpointOrNearest(WEDNESDAY, room, null))
            .thenReturn(checkpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByDayCheckpointAndType(
                DayOfWeek.WEDNESDAY,
                checkpoint,
                clubType
            )
        ).thenReturn(emptySet())
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 1))
            .thenReturn(sortMode)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 2))
            .thenReturn(null)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 3))
            .thenReturn(null)
        whenever(
            attendanceQueryRepository.findActivityRoomIdsByGradeDayAndType(1, DayOfWeek.WEDNESDAY, clubType)
        ).thenReturn(setOf(room.id!!))

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("댄스부", result[room.id])
    }

    @Test
    fun `동아리 시간도 강제 정렬도 아니면 실제 이름을 반환한다`() {
        val room = RoomEntity(id = 3L, name = "3-1", grade = 3, classNumber = 1, clubName = "합창부")
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME))
            .thenReturn(clubType)
        whenever(roomCheckpointResolver.getCurrentCheckpointOrNearest(WEDNESDAY, room, 3))
            .thenReturn(checkpoint)
        whenever(
            attendanceQueryRepository.findScheduledRoomIdsByDayCheckpointAndType(
                DayOfWeek.WEDNESDAY,
                checkpoint,
                clubType
            )
        ).thenReturn(emptySet())
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 1))
            .thenReturn(null)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 2))
            .thenReturn(null)
        whenever(attendanceSortModeRepository.findByDateAndCheckpointAndGrade(WEDNESDAY, checkpoint, 3))
            .thenReturn(null)

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("3-1", result[room.id])
    }

    companion object {
        private val WEDNESDAY: LocalDate = LocalDate.of(2026, 8, 5)
    }
}
