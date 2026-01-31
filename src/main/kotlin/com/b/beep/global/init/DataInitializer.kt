package com.b.beep.global.init

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
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
        if (checkpointRepository.count() > 0) {
            logger.info("Checkpoints already exist, skipping")
            return
        }

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
            )
        )

        checkpointData.forEach { checkpointRepository.save(it) }
        logger.info("Checkpoints initialized")
    }
}
