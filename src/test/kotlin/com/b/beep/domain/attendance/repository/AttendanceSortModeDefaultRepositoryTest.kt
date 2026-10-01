package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeDefaultEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.global.init.DataInitializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@DataJpaTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:sort-mode-defaults;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
class AttendanceSortModeDefaultRepositoryTest(
    @Autowired private val defaultRepository: AttendanceSortModeDefaultRepository,
    @Autowired private val sortModeRepository: AttendanceSortModeRepository,
    @Autowired private val checkpointRepository: AttendanceCheckpointRepository,
    @Autowired private val typeRepository: AttendanceTypeRepository,
    @Autowired private val transactionManager: PlatformTransactionManager,
) {
    @Test
    fun `기본값 조회는 요일과 체크포인트를 구분하고 삭제 타입을 제외한다`() {
        val checkpoint = saveCheckpoint("10~11교시")
        val otherCheckpoint = saveCheckpoint("최종 출석")
        val club = typeRepository.save(AttendanceTypeEntity(name = AttendanceTypeEntity.CLUB_TYPE_NAME))
        val deletedType = typeRepository.save(AttendanceTypeEntity(name = "삭제된 타입", isDeleted = true))
        saveDefault(DayOfWeek.WEDNESDAY, checkpoint, 1, club)
        saveDefault(DayOfWeek.WEDNESDAY, checkpoint, 2, club)
        saveDefault(DayOfWeek.WEDNESDAY, checkpoint, 3, deletedType)
        saveDefault(DayOfWeek.THURSDAY, checkpoint, 1, club)
        saveDefault(DayOfWeek.WEDNESDAY, otherCheckpoint, 1, club)

        val result = defaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
            DayOfWeek.WEDNESDAY, listOf(checkpoint)
        )

        assertEquals(setOf(1, 2), result.map { it.grade }.toSet())
        assertTrue(result.all { it.dayOfWeek == DayOfWeek.WEDNESDAY && it.checkpoint.id == checkpoint.id && it.type.id == club.id })
    }

    @Test
    fun `같은 요일 체크포인트 학년에 기본값을 중복 등록할 수 없다`() {
        val checkpoint = saveCheckpoint("10~11교시")
        val club = typeRepository.save(AttendanceTypeEntity(name = AttendanceTypeEntity.CLUB_TYPE_NAME))
        saveDefault(DayOfWeek.WEDNESDAY, checkpoint, 1, club)

        assertThrows<DataIntegrityViolationException> {
            saveDefault(DayOfWeek.WEDNESDAY, checkpoint, 1, club)
        }
    }

    @Test
    fun `변경 모드 여부는 출석 날짜 체크포인트 학년이 모두 일치해야 한다`() {
        val date = LocalDate.of(2026, 10, 7)
        val checkpoint = saveCheckpoint("10~11교시")
        val otherCheckpoint = saveCheckpoint("최종 출석")
        val club = typeRepository.save(AttendanceTypeEntity(name = AttendanceTypeEntity.CLUB_TYPE_NAME))
        sortModeRepository.saveAndFlush(AttendanceSortModeEntity(date = date, checkpoint = checkpoint, grade = 1, type = club))

        assertTrue(sortModeRepository.existsByDateAndCheckpointAndGrade(date, checkpoint, 1))
        assertFalse(sortModeRepository.existsByDateAndCheckpointAndGrade(date.plusDays(1), checkpoint, 1))
        assertFalse(sortModeRepository.existsByDateAndCheckpointAndGrade(date, otherCheckpoint, 1))
        assertFalse(sortModeRepository.existsByDateAndCheckpointAndGrade(date, checkpoint, 2))
    }

    @Test
    fun `수요일 초기 기본값은 학년별로 등록하고 재시작 시 기존 설정을 보존한다`() {
        val initializer = DataInitializer(
            checkpointRepository, typeRepository, defaultRepository, TransactionTemplate(transactionManager)
        )
        initializer.run(DefaultApplicationArguments())
        val checkpoint = checkpointRepository.findByNameAndIsDeletedFalse("10~11교시")!!
        val initial = defaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
            DayOfWeek.WEDNESDAY, listOf(checkpoint)
        )
        assertEquals(setOf(1, 2, 3), initial.map { it.grade }.toSet())
        assertTrue(initial.all { it.type.name == AttendanceTypeEntity.CLUB_TYPE_NAME })
        val firstGrade = initial.first { it.grade == 1 }
        firstGrade.type = typeRepository.findByNameAndIsDeletedFalse(AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME)!!
        defaultRepository.saveAndFlush(firstGrade)

        initializer.run(DefaultApplicationArguments())

        val result = defaultRepository.findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
            DayOfWeek.WEDNESDAY, listOf(checkpoint)
        )
        assertEquals(3, defaultRepository.count())
        assertEquals(AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME, result.first { it.grade == 1 }.type.name)
        assertEquals(initial.map { it.id }.toSet(), result.map { it.id }.toSet())
    }

    private fun saveCheckpoint(name: String) = checkpointRepository.save(AttendanceCheckpointEntity(
        name = name,
        startAt = LocalTime.of(19, 0), endAt = LocalTime.of(20, 39),
        attendanceStartAt = LocalTime.of(19, 0), attendanceEndAt = LocalTime.of(19, 19)
    ))

    private fun saveDefault(dayOfWeek: DayOfWeek, checkpoint: AttendanceCheckpointEntity, grade: Int, type: AttendanceTypeEntity) =
        defaultRepository.saveAndFlush(AttendanceSortModeDefaultEntity(dayOfWeek = dayOfWeek, checkpoint = checkpoint, grade = grade, type = type))
}
