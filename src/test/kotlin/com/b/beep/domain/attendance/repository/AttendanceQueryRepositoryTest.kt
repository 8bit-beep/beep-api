package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class AttendanceQueryRepositoryTest {

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var checkpointResolver: CheckpointResolver

    @Mock
    private lateinit var queryFactory: JPAQueryFactory

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    @InjectMocks
    private lateinit var attendanceQueryRepository: AttendanceQueryRepository

    @Test
    fun `겹치는 전용 체크포인트의 상태를 학년과 요일 기준으로 조회한다`() {
        val today = LocalDate.now(SEOUL_ZONE)
        val user = student()
        val specificCheckpoint = checkpoint(
            id = 2L,
            name = "1학년 전용",
            startAt = LocalTime.of(15, 20),
            endAt = LocalTime.of(17, 19),
            dayOfWeek = today.dayOfWeek,
            grade = GRADE
        )
        val generalCheckpoint = checkpoint(
            id = 1L,
            name = "일반",
            startAt = LocalTime.of(16, 30),
            endAt = LocalTime.of(18, 59)
        )
        val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        val classroomStudy = AttendanceTypeEntity(
            id = 2L,
            name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME
        )

        whenever(checkpointResolver.getCurrentAttendableCheckpointOrNull(GRADE)).thenReturn(null)
        whenever(checkpointResolver.getCurrentCheckpointOrNearest(GRADE, today.dayOfWeek))
            .thenReturn(specificCheckpoint)
        stubLegacyGeneralCheckpoint(generalCheckpoint)
        stubAttendanceByCheckpoint(
            user = user,
            date = today,
            specificCheckpoint = specificCheckpoint,
            specificType = club,
            generalCheckpoint = generalCheckpoint,
            generalType = classroomStudy
        )

        val result = attendanceQueryRepository.findCurrentStatus(user, GRADE)

        assertSame(club, result)
        verify(attendanceRepository).findByCheckpointAndUserAndDate(specificCheckpoint, user, today)
        verify(attendanceRepository, never()).findByCheckpointAndUserAndDate(generalCheckpoint, user, today)
    }

    @Test
    fun `체크포인트 시작 전 출석 가능 시간에는 출석 저장 대상의 상태를 조회한다`() {
        val today = LocalDate.now(SEOUL_ZONE)
        val user = student()
        val specificCheckpoint = checkpoint(
            id = 2L,
            name = "1학년 전용",
            startAt = LocalTime.of(17, 20),
            endAt = LocalTime.of(18, 10),
            attendanceStartAt = LocalTime.of(17, 0),
            attendanceEndAt = LocalTime.of(17, 30),
            dayOfWeek = today.dayOfWeek,
            grade = GRADE
        )
        val generalCheckpoint = checkpoint(
            id = 1L,
            name = "일반",
            startAt = LocalTime.of(16, 30),
            endAt = LocalTime.of(18, 59)
        )
        val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        val classroomStudy = AttendanceTypeEntity(
            id = 2L,
            name = AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME
        )

        whenever(checkpointResolver.getCurrentAttendableCheckpointOrNull(GRADE))
            .thenReturn(specificCheckpoint)
        stubLegacyGeneralCheckpoint(generalCheckpoint)
        stubAttendanceByCheckpoint(
            user = user,
            date = today,
            specificCheckpoint = specificCheckpoint,
            specificType = club,
            generalCheckpoint = generalCheckpoint,
            generalType = classroomStudy
        )

        val result = attendanceQueryRepository.findCurrentStatus(user, GRADE)

        assertSame(club, result)
        verify(attendanceRepository).findByCheckpointAndUserAndDate(specificCheckpoint, user, today)
        verify(attendanceRepository, never()).findByCheckpointAndUserAndDate(generalCheckpoint, user, today)
        verify(checkpointResolver, never()).getCurrentCheckpointOrNearest(GRADE, today.dayOfWeek)
    }

    private fun stubLegacyGeneralCheckpoint(generalCheckpoint: AttendanceCheckpointEntity) {
        Mockito.lenient()
            .`when`(checkpointResolver.getCurrentCheckpointOrNearest())
            .thenReturn(generalCheckpoint)
    }

    private fun stubAttendanceByCheckpoint(
        user: UserEntity,
        date: LocalDate,
        specificCheckpoint: AttendanceCheckpointEntity,
        specificType: AttendanceTypeEntity,
        generalCheckpoint: AttendanceCheckpointEntity,
        generalType: AttendanceTypeEntity
    ) {
        val specificAttendance = AttendanceEntity(
            checkpoint = specificCheckpoint,
            type = specificType,
            user = user,
            date = date
        )
        val generalAttendance = AttendanceEntity(
            checkpoint = generalCheckpoint,
            type = generalType,
            user = user,
            date = date
        )

        whenever(attendanceRepository.findByCheckpointAndUserAndDate(any(), eq(user), eq(date)))
            .thenAnswer { invocation ->
                when (invocation.getArgument<AttendanceCheckpointEntity>(0)) {
                    specificCheckpoint -> specificAttendance
                    generalCheckpoint -> generalAttendance
                    else -> null
                }
            }
    }

    private fun student(): UserEntity {
        return UserEntity(
            id = 1L,
            username = "student",
            name = "학생",
            role = UserRole.STUDENT
        )
    }

    private fun checkpoint(
        id: Long,
        name: String,
        startAt: LocalTime,
        endAt: LocalTime,
        attendanceStartAt: LocalTime = startAt,
        attendanceEndAt: LocalTime = endAt,
        dayOfWeek: DayOfWeek? = null,
        grade: Int? = null
    ): AttendanceCheckpointEntity {
        return AttendanceCheckpointEntity(
            id = id,
            name = name,
            startAt = startAt,
            endAt = endAt,
            attendanceStartAt = attendanceStartAt,
            attendanceEndAt = attendanceEndAt,
            dayOfWeek = dayOfWeek,
            grade = grade
        )
    }

    companion object {
        private const val GRADE = 1
        private val SEOUL_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
