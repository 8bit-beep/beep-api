package com.b.beep.domain.attendance.domain

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.DayOfWeek
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class CheckpointResolverTest {

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    private lateinit var checkpointResolver: CheckpointResolver
    private lateinit var checkpoints: List<AttendanceCheckpointEntity>

    @BeforeEach
    fun setUp() {
        checkpointResolver = CheckpointResolver(checkpointRepository)
        checkpoints = listOf(
            checkpoint(1L, "8~9교시", 16, 30, 18, 59),
            checkpoint(2L, "10~11교시", 19, 0, 20, 39),
            checkpoint(3L, "최종 출석", 20, 40, 21, 50),
            checkpoint(4L, "7~8교시", 15, 20, 17, 19, DayOfWeek.MONDAY, 1),
            checkpoint(5L, "9교시", 17, 20, 18, 10, DayOfWeek.MONDAY, 1)
        )
    }

    @Test
    fun `월요일 1학년은 16시에 7~8교시를 사용한다`() {
        val result = resolve(grade = 1, now = LocalTime.of(16, 0))

        assertEquals("7~8교시", result.name)
    }

    @Test
    fun `월요일 1학년은 17시 30분에 9교시를 사용한다`() {
        val result = resolve(grade = 1, now = LocalTime.of(17, 30))

        assertEquals("9교시", result.name)
    }

    @Test
    fun `월요일 2학년은 17시 30분에 일반 8~9교시를 사용한다`() {
        val result = resolve(grade = 2, now = LocalTime.of(17, 30))

        assertEquals("8~9교시", result.name)
    }

    @Test
    fun `월요일 1학년은 18시 30분에 다음 10~11교시를 사용한다`() {
        val result = resolve(grade = 1, now = LocalTime.of(18, 30))

        assertEquals("10~11교시", result.name)
    }

    private fun resolve(
        grade: Int,
        now: LocalTime
    ): AttendanceCheckpointEntity {
        return checkpointResolver.resolveCurrentCheckpointOrNearest(
            checkpoints = checkpoints,
            grade = grade,
            dayOfWeek = DayOfWeek.MONDAY,
            now = now
        )
    }

    private fun checkpoint(
        id: Long,
        name: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        dayOfWeek: DayOfWeek? = null,
        grade: Int? = null
    ): AttendanceCheckpointEntity {
        val startAt = LocalTime.of(startHour, startMinute)
        val endAt = LocalTime.of(endHour, endMinute)
        return AttendanceCheckpointEntity(
            id = id,
            name = name,
            startAt = startAt,
            endAt = endAt,
            attendanceStartAt = startAt,
            attendanceEndAt = endAt,
            dayOfWeek = dayOfWeek,
            grade = grade
        )
    }
}
