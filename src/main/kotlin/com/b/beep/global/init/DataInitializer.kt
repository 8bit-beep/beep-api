package com.b.beep.global.init

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.domain.memo.domain.entity.MemoEntity
import com.b.beep.domain.memo.repository.MemoRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val roomRepository: RoomRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val memoRepository: MemoRepository,
    private val typeRepository: AttendanceTypeRepository,
    private val transactionTemplate: org.springframework.transaction.support.TransactionTemplate,
) : ApplicationRunner {
    val logger = logger()

    override fun run(args: ApplicationArguments) {
        logger.info("DataInitializer - Checking data...")

        transactionTemplate.execute {
            createAttendanceTypes()

            if (userRepository.count() > 0) {
                logger.info("Data already exists, skipping")
                return@execute
            }

            logger.info("Creating dummy data...")
            createMemo()
            val rooms = createRooms()
            val checkpoints = createCheckpoints()
            val students = createStudents()
            createTeachers()
            createSchedules(students, rooms, checkpoints)
            logger.info("Dummy data created successfully!")
        }
    }

    private fun createAttendanceTypes() {
        val typeNames = listOf(
            "CLUB", "CLASS", "NOT_ATTEND", "SLEEPOVER", "OUTGOING", "FIELD_PRACTICE",
            "NARSHA", "AFTER_SCHOOL", "SHIFT_ATTEND", "OTHER", "INDUSTRY",
            "WINTER_CAMP_SELF_STUDY", "WINTER_CAMP_LECTURE"
        )

        typeNames.forEach { name ->
            if (typeRepository.findByNameAndIsDeletedFalse(name) == null) {
                typeRepository.save(AttendanceTypeEntity(name = name))
            }
        }
        logger.info("Attendance types initialized")
    }

    private fun createMemo() {
        memoRepository.save(MemoEntity(content = "Hello World!"))
    }

    private fun createRooms(): List<RoomEntity> {
        val roomNames = listOf(
            "1동아리실",
            "2동아리실",
            "3동아리실",
            "4동아리실",
            "5동아리실"
        )

        return roomNames.map { name ->
            roomRepository.save(RoomEntity(name = name))
        }
    }

    private fun createCheckpoints(): List<AttendanceCheckpointEntity> {
        val checkpointData = listOf(
            AttendanceCheckpointEntity(
                name = "8~9교시",
                startAt = LocalTime.of(14, 0),
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

        return checkpointData.map { checkpointRepository.save(it) }
    }

    private fun createStudents(): List<UserEntity> {
        val names = listOf(
            "김민준", "이서준", "박도윤", "최예준", "정시우",
            "강지호", "조현우", "윤서연", "장지아", "임하윤",
            "한수아", "신지원", "송윤서", "백채원", "오다은",
            "서지민", "황지유", "손은서", "유하은", "권시은"
        )

        val students = mutableListOf<UserEntity>()

        names.forEachIndexed { index, name ->
            val grade = (index / 8) + 1
            val classNumber = ((index % 8) / 2) + 1
            val num = (index % 2) + ((index / 2 % 4) * 2) + 1

            val user = userRepository.save(
                UserEntity(
                    email = "student${index + 1}@dgsw.hs.kr",
                    username = name,
                    role = UserRole.STUDENT
                )
            )

            studentInfoRepository.save(
                StudentInfoEntity(
                    user = user,
                    grade = grade,
                    classNumber = classNumber,
                    num = num,
                    cardId = "CARD${String.format("%04d", index + 1)}"
                )
            )

            students.add(user)
        }

        return students
    }

    private fun createTeachers(): List<UserEntity> {
        val teacherNames = listOf("박선생", "김선생")

        return teacherNames.mapIndexed { index, name ->
            userRepository.save(
                UserEntity(
                    email = "teacher${index + 1}@dgsw.hs.kr",
                    username = name,
                    role = UserRole.TEACHER
                )
            )
        }
    }

    private fun createSchedules(
        students: List<UserEntity>,
        rooms: List<RoomEntity>,
        checkpoints: List<AttendanceCheckpointEntity>
    ) {
        val weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY
        )

        val clubType = typeRepository.findByNameAndIsDeletedFalse("CLUB")!!

        students.forEach { student ->
            val room = rooms[student.id!!.toInt() % rooms.size]

            weekdays.forEach { day ->
                checkpoints.forEach { checkpoint ->
                    studentScheduleRepository.save(
                        StudentScheduleEntity(
                            user = student,
                            room = room,
                            dayOfWeek = day,
                            checkpoint = checkpoint,
                            type = clubType
                        )
                    )
                }
            }
        }
    }
}
