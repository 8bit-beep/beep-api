package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceSortModeRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceTypeError
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
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

        attendanceSortModeService.updateSortMode(request, MONDAY)

        verify(attendanceSortModeRepository).deleteByDateAndCheckpointAndGrade(
            MONDAY,
            firstGradeCheckpoint,
            1
        )
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
    }

    private fun stubGetSortModes() {
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
        ).thenReturn(emptyList())
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
