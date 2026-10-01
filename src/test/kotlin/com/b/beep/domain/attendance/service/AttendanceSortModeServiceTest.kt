package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceSortModeRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeDefaultEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceTypeError
import com.b.beep.domain.attendance.repository.AttendanceSortModeDefaultRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class AttendanceSortModeServiceTest {

    @Mock
    private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository

    @Mock
    private lateinit var attendanceSortModeDefaultRepository: AttendanceSortModeDefaultRepository

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    @Mock
    private lateinit var checkpointResolver: CheckpointResolver

    @InjectMocks
    private lateinit var attendanceSortModeService: AttendanceSortModeService

    private lateinit var generalCheckpoint: AttendanceCheckpointEntity
    private lateinit var firstGradeCheckpoint: AttendanceCheckpointEntity
    private lateinit var type: AttendanceTypeEntity

    @BeforeEach
    fun setUp() {
        generalCheckpoint = checkpoint(1L, "8~9교시")
        firstGradeCheckpoint = checkpoint(2L, "9교시", DayOfWeek.MONDAY, 1)
        type = AttendanceTypeEntity(id = 8L, name = "방과후")
    }

    @Test
    fun `월요일 1학년 mode는 1학년 체크포인트에 저장한다`() {
        val request = UpdateAttendanceSortModeRequest(grade = 1, typeId = type.id)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        whenever(attendanceTypeService.getAttendanceTypeEntityById(type.id!!)).thenReturn(type)
        whenever(
            attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
                MONDAY,
                firstGradeCheckpoint,
                1
            )
        ).thenReturn(null)
        stubGetSortModes()

        attendanceSortModeService.updateSortMode(request, MONDAY)

        val captor = argumentCaptor<AttendanceSortModeEntity>()
        verify(attendanceSortModeRepository).save(captor.capture())
        assertEquals(firstGradeCheckpoint, captor.firstValue.checkpoint)
        assertEquals(1, captor.firstValue.grade)
        assertEquals(type, captor.firstValue.type)
    }

    @Test
    fun `변경없음은 해당 학년 체크포인트 row를 삭제한다`() {
        val request = UpdateAttendanceSortModeRequest(grade = 1, typeId = null)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        stubGetSortModes()
        val defaultMode = AttendanceSortModeDefaultEntity(
            dayOfWeek = DayOfWeek.MONDAY,
            checkpoint = firstGradeCheckpoint,
            grade = 1,
            type = type
        )
        whenever(
            attendanceSortModeDefaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
                eq(DayOfWeek.MONDAY), any()
            )
        ).thenReturn(listOf(defaultMode))

        val result = attendanceSortModeService.updateSortMode(request, MONDAY)

        verify(attendanceSortModeRepository).deleteByDateAndCheckpointAndGrade(
            MONDAY,
            firstGradeCheckpoint,
            1
        )
        assertNull(result.modes.first { it.grade == 1 }.type)
        assertEquals(type.id, result.modes.first { it.grade == 1 }.defaultType?.id)
        verify(attendanceSortModeDefaultRepository, never()).save(any<AttendanceSortModeDefaultEntity>())
    }

    @Test
    fun `POTC는 재정렬 모드로 선택할 수 없다`() {
        val potc = AttendanceTypeEntity(id = 9L, name = "POTC")
        val request = UpdateAttendanceSortModeRequest(grade = 1, typeId = potc.id)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(1, DayOfWeek.MONDAY))
            .thenReturn(firstGradeCheckpoint)
        whenever(attendanceTypeService.getAttendanceTypeEntityById(potc.id!!)).thenReturn(potc)

        val exception = assertThrows<CustomException> {
            attendanceSortModeService.updateSortMode(request, MONDAY)
        }

        assertEquals(AttendanceTypeError.UNSUPPORTED_SORT_MODE_TYPE, exception.error)
    }

    @Test
    fun `조회 응답은 학년마다 적용 체크포인트를 포함한다`() {
        val firstGradeMode = AttendanceSortModeEntity(
            id = 1L,
            date = MONDAY,
            checkpoint = firstGradeCheckpoint,
            grade = 1,
            type = type
        )
        val checkpoints = mapOf(
            1 to firstGradeCheckpoint,
            2 to generalCheckpoint,
            3 to generalCheckpoint
        )
        whenever(checkpointResolver.getCurrentCheckpointsOrNearest(listOf(1, 2, 3), DayOfWeek.MONDAY))
            .thenReturn(checkpoints)
        whenever(
            attendanceSortModeRepository.findAllByDateAndCheckpointIn(
                eq(MONDAY),
                any()
            )
        ).thenReturn(listOf(firstGradeMode))

        val result = attendanceSortModeService.getSortModes(MONDAY)

        assertEquals("9교시", result.modes.first { it.grade == 1 }.checkpoint.name)
        assertEquals("방과후", result.modes.first { it.grade == 1 }.type?.name)
        assertEquals("8~9교시", result.modes.first { it.grade == 2 }.checkpoint.name)
        assertEquals(null, result.modes.first { it.grade == 2 }.type)
        assertNull(result.modes.first { it.grade == 2 }.defaultType)
    }

    @Test
    fun `수요일 기본 동아리는 날짜별 변경값 없이 반환한다`() {
        val wednesday = LocalDate.of(2026, 10, 7)
        val checkpoint = checkpoint(3L, "10~11교시")
        val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        whenever(checkpointResolver.getCurrentCheckpointsOrNearest(listOf(1, 2, 3), DayOfWeek.WEDNESDAY))
            .thenReturn(mapOf(1 to checkpoint, 2 to checkpoint, 3 to checkpoint))
        whenever(
            attendanceSortModeDefaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
                DayOfWeek.WEDNESDAY, listOf(checkpoint)
            )
        ).thenReturn((1..3).map { grade ->
            AttendanceSortModeDefaultEntity(dayOfWeek = DayOfWeek.WEDNESDAY, checkpoint = checkpoint, grade = grade, type = club)
        })

        val result = attendanceSortModeService.getSortModes(wednesday)

        assertEquals(wednesday, result.date)
        assertEquals(listOf(1, 2, 3), result.modes.map { it.grade })
        result.modes.forEach { mode ->
            assertNull(mode.type)
            assertEquals(club.id, mode.defaultType?.id)
            assertEquals(checkpoint.id, mode.checkpoint.id)
        }
        verify(attendanceSortModeRepository, never()).save(any<AttendanceSortModeEntity>())
    }

    @Test
    fun `학년별 전용 체크포인트의 변경값과 기본값을 구분한다`() {
        val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        stubGetSortModes(listOf(
            AttendanceSortModeEntity(date = MONDAY, checkpoint = firstGradeCheckpoint, grade = 1, type = type)
        ))
        whenever(
            attendanceSortModeDefaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
                eq(DayOfWeek.MONDAY), any()
            )
        ).thenReturn(listOf(
            AttendanceSortModeDefaultEntity(dayOfWeek = DayOfWeek.MONDAY, checkpoint = firstGradeCheckpoint, grade = 1, type = club),
            AttendanceSortModeDefaultEntity(dayOfWeek = DayOfWeek.MONDAY, checkpoint = generalCheckpoint, grade = 1, type = type),
            AttendanceSortModeDefaultEntity(dayOfWeek = DayOfWeek.MONDAY, checkpoint = generalCheckpoint, grade = 2, type = type)
        ))

        val result = attendanceSortModeService.getSortModes(MONDAY)

        val firstGrade = result.modes.first { it.grade == 1 }
        assertEquals(firstGradeCheckpoint.id, firstGrade.checkpoint.id)
        assertEquals(type.id, firstGrade.type?.id)
        assertEquals(club.id, firstGrade.defaultType?.id)
        val secondGrade = result.modes.first { it.grade == 2 }
        assertNull(secondGrade.type)
        assertEquals(type.id, secondGrade.defaultType?.id)
        assertNull(result.modes.first { it.grade == 3 }.defaultType)
    }

    @Test
    fun `재정렬에 사용할 수 없는 기본 타입은 표시하지 않는다`() {
        stubGetSortModes()
        whenever(
            attendanceSortModeDefaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
                eq(DayOfWeek.MONDAY), any()
            )
        ).thenReturn(listOf(AttendanceSortModeDefaultEntity(
            dayOfWeek = DayOfWeek.MONDAY,
            checkpoint = firstGradeCheckpoint,
            grade = 1,
            type = AttendanceTypeEntity(id = 9L, name = "POTC")
        )))

        val result = attendanceSortModeService.getSortModes(MONDAY)

        assertNull(result.modes.first { it.grade == 1 }.defaultType)
    }

    private fun stubGetSortModes(modes: List<AttendanceSortModeEntity> = emptyList()) {
        whenever(checkpointResolver.getCurrentCheckpointsOrNearest(listOf(1, 2, 3), DayOfWeek.MONDAY))
            .thenReturn(
                mapOf(
                    1 to firstGradeCheckpoint,
                    2 to generalCheckpoint,
                    3 to generalCheckpoint
                )
            )
        whenever(
            attendanceSortModeRepository.findAllByDateAndCheckpointIn(
                eq(MONDAY),
                any()
            )
        ).thenReturn(modes)
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
