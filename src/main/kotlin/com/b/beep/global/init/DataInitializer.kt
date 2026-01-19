package com.b.beep.global.init

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
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
import java.time.LocalDate
import java.time.LocalTime

@Component
// TODO: 배포 전 테스트 Auth 코드 삭제, 등록 가능한 범위는 월~목, 해당 파일 삭제
class DataInitializer(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val roomRepository: RoomRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val memoRepository: MemoRepository,
    private val typeRepository: AttendanceTypeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val transactionTemplate: org.springframework.transaction.support.TransactionTemplate,
) : ApplicationRunner {
    val logger = logger()

    override fun run(args: ApplicationArguments) {
        logger.info("DataInitializer - Checking data...")

        transactionTemplate.execute {
            val types = createAttendanceTypes()

            if (userRepository.count() > 0) {
                logger.info("Data already exists, skipping")
                return@execute
            }

            logger.info("Creating dummy data...")
            createMemo()
            val rooms = createRooms()
            val checkpoints = createCheckpoints()
            val students = createStudents()
            val teachers = createTeachers()
            createSchedules(students, rooms, checkpoints, types)
            createAttendanceRecords(students, checkpoints, rooms, types)
            logger.info("Dummy data created successfully!")
        }
    }

    private fun createAttendanceTypes(): Map<String, AttendanceTypeEntity> {
        val typeNames = listOf(
            "동아리", "교실자습", "미출석", "외박", "외출",
            "현장실습", "나르샤", "방과후", "실이동", "기타",
            "산학"
        )

        val types = mutableMapOf<String, AttendanceTypeEntity>()
        typeNames.forEach { name ->
            val existing = typeRepository.findByNameAndIsDeletedFalse(name)
            if (existing != null) {
                types[name] = existing
            } else {
                types[name] = typeRepository.save(AttendanceTypeEntity(name = name))
            }
        }
        logger.info("Attendance types initialized")
        return types
    }

    private fun createMemo() {
        memoRepository.save(MemoEntity(content = "1학년 수련회 기간(11/20~22) 외박 처리 필요"))
    }

    private fun createRooms(): List<RoomEntity> {
        data class RoomData(val name: String, val floor: Int)

        val roomData = listOf(
            RoomData("세미나실", 1),
            RoomData("소회의실", 1),
            RoomData("자습실 A", 2),
            RoomData("자습실 B", 2),
            RoomData("프로젝트실", 3)
        )

        return roomData.map { data ->
            roomRepository.save(RoomEntity(name = data.name, floor = data.floor))
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
        // 실제 같은 한국 이름 40명 (1학년~3학년, 각 반 3~4명)
        data class StudentData(val name: String, val grade: Int, val classNum: Int, val num: Int)

        val studentDataList = listOf(
            // 1학년 1반 (4명)
            StudentData("김태현", 1, 1, 1),
            StudentData("박서진", 1, 1, 5),
            StudentData("이도현", 1, 1, 9),
            StudentData("정하린", 1, 1, 13),

            // 1학년 2반 (3명)
            StudentData("최유진", 1, 2, 2),
            StudentData("한소율", 1, 2, 7),
            StudentData("오승현", 1, 2, 12),

            // 1학년 3반 (3명)
            StudentData("신예은", 1, 3, 3),
            StudentData("임지훈", 1, 3, 8),
            StudentData("강민서", 1, 3, 14),

            // 1학년 4반 (4명)
            StudentData("김지안", 1, 4, 1),
            StudentData("김채원", 1, 4, 6),
            StudentData("김현준", 1, 4, 10),
            StudentData("이수빈", 1, 4, 15),

            // 2학년 1반 (3명)
            StudentData("김우진", 2, 1, 2),
            StudentData("권나연", 2, 1, 8),
            StudentData("이민재", 2, 1, 13),

            // 2학년 2반 (4명)
            StudentData("이예린", 2, 2, 1),
            StudentData("안성민", 2, 2, 5),
            StudentData("이하준", 2, 2, 11),
            StudentData("김지영", 2, 2, 16),

            // 2학년 3반 (3명)
            StudentData("전서현", 2, 3, 3),
            StudentData("홍준혁", 2, 3, 9),
            StudentData("손다인", 2, 3, 14),

            // 2학년 4반 (3명)
            StudentData("류민지", 2, 4, 4),
            StudentData("정현서", 2, 4, 10),
            StudentData("김시우", 2, 4, 15),

            // 3학년 1반 (4명)
            StudentData("이준영", 3, 1, 1),
            StudentData("박하은", 3, 1, 6),
            StudentData("최동현", 3, 1, 11),
            StudentData("강서윤", 3, 1, 17),

            // 3학년 2반 (3명)
            StudentData("임지아", 3, 2, 2),
            StudentData("한승우", 3, 2, 8),
            StudentData("오채영", 3, 2, 13),

            // 3학년 3반 (3명)
            StudentData("신민혁", 3, 3, 4),
            StudentData("서유나", 3, 3, 9),
            StudentData("윤재민", 3, 3, 15),

            // 3학년 4반 (3명)
            StudentData("문소희", 3, 4, 3),
            StudentData("배준서", 3, 4, 10),
            StudentData("장다현", 3, 4, 16)
        )

        val students = mutableListOf<UserEntity>()

        studentDataList.forEachIndexed { index, data ->
            val user = userRepository.save(
                UserEntity(
                    email = "student${data.grade}${data.classNum}${String.format("%02d", data.num)}@dgsw.hs.kr",
                    username = data.name,
                    role = UserRole.STUDENT
                )
            )

            studentInfoRepository.save(
                StudentInfoEntity(
                    user = user,
                    grade = data.grade,
                    classNumber = data.classNum,
                    num = data.num,
                    cardId = "CARD${data.grade}${data.classNum}${String.format("%02d", data.num)}"
                )
            )

            students.add(user)
        }

        return students
    }

    private fun createTeachers(): List<UserEntity> {
        val teacherData = listOf(
            "김영수" to "담임(1-1)",
            "박지현" to "담임(1-2)",
            "이민호" to "담임(2-1)",
            "최수진" to "담임(2-2)",
            "정대현" to "야자담당"
        )

        return teacherData.mapIndexed { index, (name, _) ->
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
        checkpoints: List<AttendanceCheckpointEntity>,
        types: Map<String, AttendanceTypeEntity>
    ) {
        val weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY
        )

        // 학생별 스케줄 패턴 정의 (현실적인 다양한 케이스)
        data class SchedulePattern(
            val defaultType: String,
            val defaultRoom: Int,  // rooms 인덱스
            val exceptions: Map<DayOfWeek, Pair<String, Int>>  // 요일별 예외
        )

        val clubType = types["동아리"]!!
        val classStudy = types["교실자습"]!!
        val narsha = types["나르샤"]!!
        val afterSchool = types["방과후"]!!
        val fieldTraining = types["현장실습"]!!

        students.forEachIndexed { studentIndex, student ->
            // 학생마다 다른 스케줄 패턴
            val pattern = when (studentIndex % 8) {
                0 -> SchedulePattern("동아리", 0, emptyMap())  // 1층 세미나실 - 동아리만
                1 -> SchedulePattern("동아리", 1, emptyMap())  // 1층 소회의실 - 동아리만
                2 -> SchedulePattern("동아리", 2, mapOf(       // 2층 자습실 A - 수요일만 나르샤
                    DayOfWeek.WEDNESDAY to Pair("나르샤", 4)
                ))
                3 -> SchedulePattern("교실자습", 3, emptyMap()) // 2층 자습실 B - 교실자습
                4 -> SchedulePattern("동아리", 4, mapOf(       // 3층 프로젝트실 - 목요일 방과후
                    DayOfWeek.THURSDAY to Pair("방과후", 2)
                ))
                5 -> SchedulePattern("나르샤", 4, mapOf(       // 3층 프로젝트실 - 나르샤, 월요일만 동아리
                    DayOfWeek.MONDAY to Pair("동아리", 0)
                ))
                6 -> SchedulePattern("동아리", 2, mapOf(       // 2층 자습실 A - 화요일 실이동
                    DayOfWeek.TUESDAY to Pair("실이동", 3)
                ))
                else -> SchedulePattern("교실자습", 3, mapOf(  // 2층 자습실 B - 수목 나르샤
                    DayOfWeek.WEDNESDAY to Pair("나르샤", 4),
                    DayOfWeek.THURSDAY to Pair("나르샤", 4)
                ))
            }

            // 3학년 일부는 현장실습 (실제 상황 반영)
            val studentInfo = studentInfoRepository.findByUser(student)
            val is3rdGradeFieldTraining = studentInfo?.grade == 3 && studentIndex % 5 == 0

            weekdays.forEach { day ->
                checkpoints.forEach { checkpoint ->
                    val (typeName, roomIndex) = if (is3rdGradeFieldTraining) {
                        "현장실습" to 3
                    } else {
                        pattern.exceptions[day] ?: (pattern.defaultType to pattern.defaultRoom)
                    }

                    studentScheduleRepository.save(
                        StudentScheduleEntity(
                            user = student,
                            room = rooms[roomIndex],
                            dayOfWeek = day,
                            checkpoint = checkpoint,
                            type = types[typeName]!!
                        )
                    )
                }
            }
        }
    }

    private fun createAttendanceRecords(
        students: List<UserEntity>,
        checkpoints: List<AttendanceCheckpointEntity>,
        rooms: List<RoomEntity>,
        types: Map<String, AttendanceTypeEntity>
    ) {
        val today = LocalDate.now()

        // 오늘의 출석 기록 생성
        students.forEachIndexed { index, student ->
            val studentInfo = studentInfoRepository.findByUser(student)

            checkpoints.forEachIndexed { cpIndex, checkpoint ->
                // 다양한 출석 상황 시뮬레이션
                val isAbsent = index % 12 == 0 && cpIndex == 2  // 일부 학생 최종출석 미출석
                val isLate = index % 8 == 0 && cpIndex > 0       // 일부 학생 지각
                val notYetAttended = cpIndex == 2 && index >= 25 // 마지막 체크포인트는 일부만 출석

                if (!isAbsent && !notYetAttended) {
                    val attendedAt = if (isLate) {
                        // 지각: 출석 마감 시간 + 5~15분
                        checkpoint.attendanceEndAt.plusMinutes((5..15).random().toLong())
                    } else {
                        // 정상 출석: 출석 시작~마감 사이
                        checkpoint.attendanceStartAt.plusMinutes((1..10).random().toLong())
                    }

                    attendanceRepository.save(
                        AttendanceEntity(
                            user = student,
                            checkpoint = checkpoint,
                            room = rooms[index % rooms.size],
                            type = types["동아리"]!!,
                            date = today,
                        )
                    )
                }
            }
        }

        logger.info("Created attendance records for ${today}")
    }
}