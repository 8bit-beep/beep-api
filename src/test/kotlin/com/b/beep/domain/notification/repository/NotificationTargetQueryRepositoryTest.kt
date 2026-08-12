package com.b.beep.domain.notification.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.global.config.QueryDslConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DataJpaTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:notification-target;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
@Import(QueryDslConfig::class, NotificationTargetQueryRepository::class)
class NotificationTargetQueryRepositoryTest(
    @Autowired private val repository: NotificationTargetQueryRepository,
    @Autowired private val entityManager: jakarta.persistence.EntityManager,
) {
    @MockitoBean
    private lateinit var checkpointResolver: CheckpointResolver

    @Test
    fun `finds public ids of active first and second grade students`() {
        saveStudent("grade-1", grade = 1)
        saveStudent("grade-2", grade = 2)
        saveStudent("grade-3", grade = 3)
        saveStudent("deleted", grade = 1, isDeleted = true)
        saveStudent(null, grade = 1)
        entityManager.flush()

        val result = repository.findAllActiveStudentPublicIds()

        assertThat(result).containsExactlyInAnyOrder("grade-1", "grade-2")
    }

    @Test
    fun `excludes attended and actively absent students from reminder targets`() {
        val checkpoint = saveCheckpoint()
        whenever(checkpointResolver.getCurrentCheckpointOrNull()).thenReturn(checkpoint)
        val attendanceType = persistAndReturn(AttendanceTypeEntity(name = "정상 출석"))
        val missing = saveStudent("missing", grade = 1)
        val attended = saveStudent("attended", grade = 1)
        val absent = saveStudent("absent", grade = 2)
        val deletedAbsence = saveStudent("deleted-absence", grade = 2)
        saveStudent("third-grade", grade = 3)
        val today = LocalDate.now()

        entityManager.persist(
            withAudit(
                AttendanceEntity(
                    checkpoint = checkpoint,
                    type = attendanceType,
                    user = attended,
                    date = today,
                )
            )
        )
        saveAbsence(absent, today, isDeleted = false, attendanceType = attendanceType)
        saveAbsence(deletedAbsence, today, isDeleted = true, attendanceType = attendanceType)
        entityManager.flush()

        val result = repository.findAllNotAttendedPublicIds()

        assertThat(result).containsExactlyInAnyOrder(missing.publicId, deletedAbsence.publicId)
    }

    @Test
    fun `returns no reminder targets when there is no current checkpoint`() {
        whenever(checkpointResolver.getCurrentCheckpointOrNull()).thenReturn(null)
        saveStudent("missing", grade = 1)
        entityManager.flush()

        assertThat(repository.findAllNotAttendedPublicIds()).isEmpty()
    }

    private fun saveStudent(
        publicId: String?,
        grade: Int,
        isDeleted: Boolean = false,
    ): UserEntity {
        val suffix = publicId ?: "no-public-id"
        val user = persistAndReturn(
            withAudit(
                UserEntity(
                    publicId = publicId,
                    username = "user-$suffix",
                    name = suffix,
                    role = UserRole.STUDENT,
                    isDeleted = isDeleted,
                )
            )
        )
        entityManager.persist(
            StudentInfoEntity(
                user = user,
                grade = grade,
                classNumber = 1,
                num = grade,
            )
        )
        return user
    }

    private fun saveCheckpoint(): AttendanceCheckpointEntity = persistAndReturn(
        AttendanceCheckpointEntity(
            name = "저녁",
            startAt = LocalTime.of(20, 0),
            endAt = LocalTime.of(22, 0),
            attendanceStartAt = LocalTime.of(20, 0),
            attendanceEndAt = LocalTime.of(21, 0),
        )
    )

    private fun saveAbsence(
        user: UserEntity,
        today: LocalDate,
        isDeleted: Boolean,
        attendanceType: AttendanceTypeEntity,
    ) {
        val absence = persistAndReturn(
            withAudit(
                AbsenceEntity(
                    startDate = today.minusDays(1),
                    endDate = today.plusDays(1),
                    reason = "테스트",
                    type = attendanceType,
                    isDeleted = isDeleted,
                )
            )
        )
        entityManager.persist(AbsenceUserEntity(user = user, absence = absence))
    }

    private fun <T : com.b.beep.global.common.entity.BaseEntity> withAudit(entity: T): T = entity.apply {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    private fun <T : Any> persistAndReturn(entity: T): T {
        entityManager.persist(entity)
        return entity
    }
}
