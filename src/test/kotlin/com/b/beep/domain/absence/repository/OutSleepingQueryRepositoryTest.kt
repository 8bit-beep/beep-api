package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.global.common.entity.BaseEntity
import com.b.beep.global.config.QueryDslConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DataJpaTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:out-sleeping;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
@Import(QueryDslConfig::class, OutSleepingQueryRepository::class)
class OutSleepingQueryRepositoryTest(
    @Autowired private val repository: OutSleepingQueryRepository,
    @Autowired private val entityManager: EntityManager,
) {
    @Test
    fun `외박 등록과 수동 외박 출석을 학생별로 중복 없이 조회한다`() {
        val date = LocalDate.of(2026, 5, 20)
        val outSleepingType = saveType(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME)
        val firstCheckpoint = saveCheckpoint("1차")
        val secondCheckpoint = saveCheckpoint("2차")
        val manualStudent = saveStudent(null, "수동 학생", grade = 1, room = 2, number = 3)
        val managedStudent = saveStudent("managed-public-id", "등록 학생", grade = 2, room = 1, number = 4)
        val duplicatedStudent = saveStudent("duplicated-public-id", "중복 학생", grade = 3, room = 1, number = 1)

        saveAttendance(manualStudent, firstCheckpoint, outSleepingType, date)
        saveAttendance(manualStudent, secondCheckpoint, outSleepingType, date)
        saveAbsence(managedStudent, outSleepingType, date.minusDays(1), date.plusDays(1))
        saveAbsence(duplicatedStudent, outSleepingType, date, date)
        saveAttendance(duplicatedStudent, firstCheckpoint, outSleepingType, date)
        entityManager.flush()

        val result = repository.findAllStudents(date)

        assertThat(result).containsExactly(
            OutSleepingStudentQueryResult(null, "수동 학생", 1, 2, 3),
            OutSleepingStudentQueryResult("managed-public-id", "등록 학생", 2, 1, 4),
            OutSleepingStudentQueryResult("duplicated-public-id", "중복 학생", 3, 1, 1),
        )
    }

    @Test
    fun `조회 조건에 맞지 않는 외박 등록과 출석은 제외한다`() {
        val date = LocalDate.of(2026, 5, 20)
        val outSleepingType = saveType(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME)
        val otherType = saveType("외출")
        val checkpoint = saveCheckpoint("저녁")

        saveAbsence(
            saveStudent("expired", "기간 종료", 1, 1, 1),
            outSleepingType,
            date.minusDays(2),
            date.minusDays(1),
        )
        saveAbsence(
            saveStudent("deleted-absence", "삭제 등록", 1, 1, 2),
            outSleepingType,
            date,
            date,
            isDeleted = true,
        )
        saveAbsence(
            saveStudent("other-absence", "다른 등록", 1, 1, 3),
            otherType,
            date,
            date,
        )
        saveAbsence(
            saveStudent("deleted-user", "삭제 학생", 1, 1, 4, isDeleted = true),
            outSleepingType,
            date,
            date,
        )
        saveAttendance(
            saveStudent("other-date", "다른 날짜", 1, 1, 5),
            checkpoint,
            outSleepingType,
            date.minusDays(1),
        )
        saveAttendance(
            saveStudent("other-attendance", "다른 출석", 1, 1, 6),
            checkpoint,
            otherType,
            date,
        )
        val studentWithoutInfo = saveUser("no-info", "학적 없음", UserRole.STUDENT)
        saveAttendance(studentWithoutInfo, checkpoint, outSleepingType, date)
        val teacher = saveUser("teacher", "교사", UserRole.TEACHER)
        saveStudentInfo(teacher, 1, 1, 7)
        saveAttendance(teacher, checkpoint, outSleepingType, date)
        entityManager.flush()

        assertThat(repository.findAllStudents(date)).isEmpty()
    }

    private fun saveStudent(
        publicId: String?,
        name: String,
        grade: Int,
        room: Int,
        number: Int,
        isDeleted: Boolean = false,
    ): UserEntity {
        val user = saveUser(publicId, name, UserRole.STUDENT, isDeleted)
        saveStudentInfo(user, grade, room, number)
        return user
    }

    private fun saveUser(
        publicId: String?,
        name: String,
        role: UserRole,
        isDeleted: Boolean = false,
    ): UserEntity {
        val suffix = publicId ?: name
        return persist(
            withAudit(
                UserEntity(
                    publicId = publicId,
                    username = "user-$suffix",
                    name = name,
                    role = role,
                    isDeleted = isDeleted,
                )
            )
        )
    }

    private fun saveStudentInfo(user: UserEntity, grade: Int, room: Int, number: Int) {
        entityManager.persist(
            StudentInfoEntity(
                user = user,
                grade = grade,
                classNumber = room,
                num = number,
            )
        )
    }

    private fun saveType(name: String): AttendanceTypeEntity = persist(AttendanceTypeEntity(name = name))

    private fun saveCheckpoint(name: String): AttendanceCheckpointEntity = persist(
        AttendanceCheckpointEntity(
            name = name,
            startAt = LocalTime.of(20, 0),
            endAt = LocalTime.of(21, 0),
            attendanceStartAt = LocalTime.of(19, 50),
            attendanceEndAt = LocalTime.of(20, 10),
        )
    )

    private fun saveAbsence(
        user: UserEntity,
        type: AttendanceTypeEntity,
        startDate: LocalDate,
        endDate: LocalDate,
        isDeleted: Boolean = false,
    ) {
        val absence = persist(
            withAudit(
                AbsenceEntity(
                    startDate = startDate,
                    endDate = endDate,
                    reason = "테스트",
                    type = type,
                    isDeleted = isDeleted,
                )
            )
        )
        entityManager.persist(AbsenceUserEntity(user = user, absence = absence))
    }

    private fun saveAttendance(
        user: UserEntity,
        checkpoint: AttendanceCheckpointEntity,
        type: AttendanceTypeEntity,
        date: LocalDate,
    ) {
        entityManager.persist(
            withAudit(
                AttendanceEntity(
                    checkpoint = checkpoint,
                    type = type,
                    user = user,
                    date = date,
                )
            )
        )
    }

    private fun <T : BaseEntity> withAudit(entity: T): T = entity.apply {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    private fun <T : Any> persist(entity: T): T {
        entityManager.persist(entity)
        return entity
    }
}
