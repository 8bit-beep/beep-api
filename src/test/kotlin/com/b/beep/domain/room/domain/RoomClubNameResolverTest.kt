package com.b.beep.domain.room.domain

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeDefaultEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceSortModeDefaultRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class RoomClubNameResolverTest {

    @Mock
    private lateinit var checkpointResolver: CheckpointResolver

    @Mock
    private lateinit var attendanceSortModeDefaultRepository: AttendanceSortModeDefaultRepository

    @Mock
    private lateinit var attendanceSortModeRepository: AttendanceSortModeRepository

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    @InjectMocks
    private lateinit var resolver: RoomClubNameResolver

    private lateinit var checkpoint: AttendanceCheckpointEntity
    private lateinit var clubType: AttendanceTypeEntity
    private val classroomStudyType = AttendanceTypeEntity(id = 2L, name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["  "])
    fun `동아리명이 없는 실은 조회 없이 실제 이름을 그대로 반환한다`(clubName: String?) {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, classNumber = 2, clubName = clubName)

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
        verifyNoInteractions(checkpointResolver, attendanceSortModeRepository, attendanceSortModeDefaultRepository, attendanceTypeService)
    }

    @Test
    fun `변경값이 없고 기본 스케줄이 동아리이면 동아리명을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes(defaults = listOf(defaultMode(1, clubType)))

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("밴드부", result[room.id])
        assertEquals("1-2", room.name)
    }

    @Test
    fun `기본 스케줄이 교실자습이어도 변경값이 동아리이면 동아리명을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes(
            defaults = listOf(defaultMode(1, classroomStudyType)),
            modes = listOf(sortMode(1, clubType))
        )

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("밴드부", result[room.id])
    }

    @ParameterizedTest
    @ValueSource(strings = ["교실자습", "나르샤", "방과후"])
    fun `기본 스케줄이 동아리여도 다른 변경값이 있으면 실제 이름을 반환한다`(typeName: String) {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes(
            defaults = listOf(defaultMode(1, clubType)),
            modes = listOf(sortMode(1, AttendanceTypeEntity(id = 7L, name = typeName)))
        )

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
    }

    @Test
    fun `변경값이 없고 기본 스케줄이 동아리가 아니면 실제 이름을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes(defaults = listOf(defaultMode(1, classroomStudyType)))

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
    }

    @Test
    fun `변경값과 기본값이 모두 없으면 실제 이름을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes()

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
    }

    @Test
    fun `학년이 있는 실은 해당 학년만 확인하고 공용 실은 동아리 학년이 있으면 동아리명을 반환한다`() {
        val firstGradeRoom = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        val secondGradeRoom = RoomEntity(id = 2L, name = "2-1", grade = 2, clubName = "댄스부")
        val sharedRoom = RoomEntity(id = 3L, name = "프로젝트실", grade = null, clubName = "합창부")
        val unnamedRoom = RoomEntity(id = 4L, name = "일반실", grade = null)
        stubModes(defaults = listOf(defaultMode(1, classroomStudyType), defaultMode(2, clubType)))

        val result = resolver.resolveDisplayNames(listOf(firstGradeRoom, secondGradeRoom, sharedRoom, unnamedRoom), WEDNESDAY)

        assertEquals("1-2", result[firstGradeRoom.id])
        assertEquals("댄스부", result[secondGradeRoom.id])
        assertEquals("합창부", result[sharedRoom.id])
        assertEquals("일반실", result[unnamedRoom.id])
    }

    @Test
    fun `공용 실도 모든 학년의 동아리 기본값이 다른 변경값으로 바뀌면 실제 이름을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "프로젝트실", grade = null, clubName = "밴드부")
        stubModes(
            defaults = (1..3).map { defaultMode(it, clubType) },
            modes = (1..3).map { sortMode(it, classroomStudyType) }
        )

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("프로젝트실", result[room.id])
    }

    @Test
    fun `기본값 없이 동아리 변경값만 있는 공용 실은 동아리명을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "프로젝트실", grade = null, clubName = "밴드부")
        stubModes(modes = listOf(sortMode(2, clubType)))

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("밴드부", result[room.id])
    }

    @Test
    fun `학년별 현재 체크포인트에 일치하는 변경값과 기본값만 적용한다`() {
        val monday = LocalDate.of(2026, 8, 3)
        val specificCheckpoint = AttendanceCheckpointEntity(
            id = 2L, name = "1학년 전용", grade = 1, dayOfWeek = DayOfWeek.MONDAY,
            startAt = LocalTime.of(17, 20), endAt = LocalTime.of(18, 10),
            attendanceStartAt = LocalTime.of(17, 20), attendanceEndAt = LocalTime.of(17, 45)
        )
        val firstGradeRoom = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        val secondGradeRoom = RoomEntity(id = 2L, name = "2-1", grade = 2, clubName = "댄스부")
        stubModes(
            date = monday,
            checkpointByGrade = mapOf(1 to specificCheckpoint, 2 to checkpoint, 3 to checkpoint),
            defaults = listOf(
                defaultMode(1, classroomStudyType, specificCheckpoint, DayOfWeek.MONDAY),
                defaultMode(1, clubType, checkpoint, DayOfWeek.MONDAY),
                defaultMode(2, clubType, checkpoint, DayOfWeek.MONDAY)
            ),
            modes = listOf(sortMode(1, clubType, checkpoint, monday))
        )

        val result = resolver.resolveDisplayNames(listOf(firstGradeRoom, secondGradeRoom), monday)

        assertEquals("1-2", result[firstGradeRoom.id])
        assertEquals("댄스부", result[secondGradeRoom.id])
    }

    @Test
    fun `재정렬에 지원하지 않는 기본 타입은 실제 이름을 반환한다`() {
        val room = RoomEntity(id = 1L, name = "1-2", grade = 1, clubName = "밴드부")
        stubModes(defaults = listOf(defaultMode(1, AttendanceTypeEntity(id = 9L, name = "POTC"))))

        val result = resolver.resolveDisplayNames(listOf(room), WEDNESDAY)

        assertEquals("1-2", result[room.id])
    }

    private fun stubModes(
        date: LocalDate = WEDNESDAY,
        checkpointByGrade: Map<Int, AttendanceCheckpointEntity> = mapOf(1 to checkpoint, 2 to checkpoint, 3 to checkpoint),
        defaults: List<AttendanceSortModeDefaultEntity> = emptyList(),
        modes: List<AttendanceSortModeEntity> = emptyList()
    ) {
        val checkpoints = checkpointByGrade.values.distinctBy { it.id }
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME))
            .thenReturn(clubType)
        whenever(checkpointResolver.getCurrentCheckpointsOrNearest(listOf(1, 2, 3), date.dayOfWeek))
            .thenReturn(checkpointByGrade)
        whenever(attendanceSortModeRepository.findAllByDateAndCheckpointIn(date, checkpoints)).thenReturn(modes)
        whenever(attendanceSortModeDefaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(date.dayOfWeek, checkpoints))
            .thenReturn(defaults)
    }

    private fun defaultMode(
        grade: Int,
        type: AttendanceTypeEntity,
        checkpoint: AttendanceCheckpointEntity = this.checkpoint,
        dayOfWeek: DayOfWeek = DayOfWeek.WEDNESDAY
    ) = AttendanceSortModeDefaultEntity(dayOfWeek = dayOfWeek, checkpoint = checkpoint, grade = grade, type = type)

    private fun sortMode(
        grade: Int,
        type: AttendanceTypeEntity,
        checkpoint: AttendanceCheckpointEntity = this.checkpoint,
        date: LocalDate = WEDNESDAY
    ) = AttendanceSortModeEntity(date = date, checkpoint = checkpoint, grade = grade, type = type)

    companion object {
        private val WEDNESDAY: LocalDate = LocalDate.of(2026, 8, 5)
    }
}
