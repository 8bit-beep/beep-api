package com.b.beep.global.init

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime

@Component
class DataInitializer(
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val typeRepository: AttendanceTypeRepository,
    private val transactionTemplate: org.springframework.transaction.support.TransactionTemplate,
) : ApplicationRunner {
    val logger = logger()

    override fun run(args: ApplicationArguments) {
        logger.info("DataInitializer - Checking data...")

        transactionTemplate.execute {
            createAttendanceTypes()
            createCheckpoints()
        }
    }

    private fun createAttendanceTypes() {
        val typeNames = listOf(
            "동아리", "교실자습", "미출석", "외박", "외출",
            "현장실습", "나르샤", "방과후", "실이동", "기타",
            "산학", "POTC"
        )

        typeNames.forEach { name ->
            if (typeRepository.findByNameAndIsDeletedFalse(name) == null) {
                typeRepository.save(AttendanceTypeEntity(name = name))
            }
        }
        logger.info("Attendance types initialized")
    }

    private fun createCheckpoints() {
        val checkpointData = listOf(
            AttendanceCheckpointEntity(
                name = "8~9교시",
                startAt = LocalTime.of(16, 30),
                endAt = LocalTime.of(18, 59),
                attendanceStartAt = LocalTime.of(14, 0),
                attendanceEndAt = LocalTime.of(18, 39)
            ),
            AttendanceCheckpointEntity(
                name = "10~11교시",
                startAt = LocalTime.of(19, 0),
                endAt = LocalTime.of(20, 39),
                attendanceStartAt = LocalTime.of(19, 0),
                attendanceEndAt = LocalTime.of(19, 19)
            ),
            AttendanceCheckpointEntity(
                name = "최종 출석",
                startAt = LocalTime.of(20, 40),
                endAt = LocalTime.of(21, 50),
                attendanceStartAt = LocalTime.of(20, 40),
                attendanceEndAt = LocalTime.of(20, 59)
            ),
            AttendanceCheckpointEntity(
                name = "7~8교시",
                startAt = LocalTime.of(15, 20),
                endAt = LocalTime.of(17, 19),
                attendanceStartAt = LocalTime.of(15, 20),
                attendanceEndAt = LocalTime.of(15, 45),
                dayOfWeek = DayOfWeek.MONDAY,
                grade = 1
            ),
            AttendanceCheckpointEntity(
                name = "9교시",
                startAt = LocalTime.of(17, 20),
                endAt = LocalTime.of(18, 10),
                attendanceStartAt = LocalTime.of(17, 20),
                attendanceEndAt = LocalTime.of(17, 45),
                dayOfWeek = DayOfWeek.MONDAY,
                grade = 1
            )
        )

        checkpointData.forEach { data ->
            if (checkpointRepository.findByNameAndIsDeletedFalse(data.name) == null) {
                checkpointRepository.save(data)
            }
        }
        logger.info("Checkpoints initialized")
    }
}
