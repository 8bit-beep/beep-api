package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.notification.domain.entity.FcmTokenEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.service.UserService
import com.b.beep.global.common.entity.BaseEntity
import com.b.beep.global.config.QueryDslConfig
import com.b.beep.global.security.ContextHolder
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@DataJpaTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:withdrawn-attendance;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
@Import(QueryDslConfig::class, AttendanceQueryRepository::class, UserService::class)
class WithdrawnStudentAttendanceTest(
    @Autowired private val repository: AttendanceQueryRepository,
    @Autowired private val userService: UserService,
    @Autowired private val entityManager: EntityManager,
) {
    @MockitoBean
    private lateinit var contextHolder: ContextHolder

    @MockitoBean
    private lateinit var checkpointResolver: CheckpointResolver

    @Test
    fun `소프트 삭제 후에도 학생 정보와 출석 기록을 조회할 수 있다`() {
        val date = today().minusDays(1)
        val student = saveStudent(1)
        val otherStudent = saveStudent(2)
        val checkpoint = saveCheckpoint("저녁")
        val type = save(AttendanceTypeEntity(name = "출석"))
        val room = save(RoomEntity(name = "자습실", floor = 1))
        val attendance = saveAttendance(student, checkpoint, type, date, room)
        val schedule = save(StudentScheduleEntity(
            user = student.user, dayOfWeek = date.dayOfWeek, checkpoint = checkpoint, type = type, room = room
        ))
        val activityRoom = save(StudentActivityRoomEntity(
            user = student.user, dayOfWeek = null, type = type, room = room
        ))
        val token = save(FcmTokenEntity(user = student.user, token = "withdrawn-token", device = "test"))
        val otherSchedule = save(StudentScheduleEntity(
            user = otherStudent.user, dayOfWeek = date.dayOfWeek, checkpoint = checkpoint, type = type, room = room
        ))
        val otherActivityRoom = save(StudentActivityRoomEntity(
            user = otherStudent.user, dayOfWeek = null, type = type, room = room
        ))
        val otherToken = save(FcmTokenEntity(user = otherStudent.user, token = "active-token", device = "test"))
        entityManager.flush()

        userService.deleteUser(student.user.id!!)
        entityManager.flush()
        entityManager.clear()

        val preservedUser = entityManager.find(UserEntity::class.java, student.user.id)
        val preservedStudent = entityManager.find(StudentInfoEntity::class.java, student.id)
        val preservedAttendance = entityManager.find(AttendanceEntity::class.java, attendance.id)
        assertThat(preservedUser.isDeleted).isTrue()
        assertThat(preservedUser.name).isEqualTo(student.user.name)
        assertThat(preservedStudent.num).isEqualTo(student.num)
        assertThat(preservedAttendance.user.id).isEqualTo(student.user.id)
        assertThat(entityManager.find(StudentScheduleEntity::class.java, schedule.id)).isNull()
        assertThat(entityManager.find(StudentActivityRoomEntity::class.java, activityRoom.id)).isNull()
        assertThat(entityManager.find(FcmTokenEntity::class.java, token.id)).isNull()
        assertThat(entityManager.find(StudentScheduleEntity::class.java, otherSchedule.id)).isNotNull()
        assertThat(entityManager.find(StudentActivityRoomEntity::class.java, otherActivityRoom.id)).isNotNull()
        assertThat(entityManager.find(FcmTokenEntity::class.java, otherToken.id)).isNotNull()
        assertThat(repository.findAllByFilters(date, checkpoint, isCurrentCheckpoint = false).map { it.id })
            .contains(student.user.id)
    }

    @Test
    fun `과거 명단은 조회 날짜와 체크포인트에 출석한 자퇴생만 포함한다`() {
        val date = today().minusDays(1)
        val active = saveStudent(1)
        val recorded = saveStudent(2, isDeleted = true)
        saveStudent(3, isDeleted = true)
        val otherDate = saveStudent(4, isDeleted = true)
        val otherCheckpoint = saveStudent(5, isDeleted = true)
        val checkpoint = saveCheckpoint("저녁")
        val secondCheckpoint = saveCheckpoint("야간")
        val type = save(AttendanceTypeEntity(name = "출석"))
        saveAttendance(recorded, checkpoint, type, date)
        saveAttendance(recorded, secondCheckpoint, type, date)
        saveAttendance(otherDate, checkpoint, type, date.minusDays(1))
        saveAttendance(otherCheckpoint, secondCheckpoint, type, date)
        entityManager.flush()

        val checkpointUsers = repository.findAllByFilters(date, checkpoint, isCurrentCheckpoint = false)
        val allCheckpointUsers = repository.findAllByFilters(date, isCurrentCheckpoint = false)

        assertThat(checkpointUsers.map { it.id }).containsExactly(active.user.id, recorded.user.id)
        assertThat(allCheckpointUsers.map { it.id })
            .containsExactly(active.user.id, recorded.user.id, otherCheckpoint.user.id)
    }

    @Test
    fun `오늘과 미래 명단에서는 출석 기록이 있어도 자퇴생을 제외한다`() {
        val active = saveStudent(1)
        val withdrawn = saveStudent(2, isDeleted = true)
        val checkpoint = saveCheckpoint("저녁")
        val type = save(AttendanceTypeEntity(name = "출석"))
        saveAttendance(withdrawn, checkpoint, type, today())
        saveAttendance(withdrawn, checkpoint, type, today().plusDays(1))
        entityManager.flush()

        listOf(null, today(), today().plusDays(1)).forEach { date ->
            assertThat(repository.findAllByFilters(date, isCurrentCheckpoint = false).map { it.id })
                .containsExactly(active.user.id)
        }
    }

    @Test
    fun `시간표가 없는 자퇴생도 과거에 실제 출석한 출석실로 조회된다`() {
        val date = today().minusDays(1)
        val student = saveStudent(1, isDeleted = true)
        val otherRoomStudent = saveStudent(2, isDeleted = true)
        val checkpoint = saveCheckpoint("저녁")
        val type = save(AttendanceTypeEntity(name = "출석"))
        val room = save(RoomEntity(name = "자습실", floor = 1))
        val otherRoom = save(RoomEntity(name = "다른 자습실", floor = 2))
        saveAttendance(student, checkpoint, type, date, room)
        saveAttendance(otherRoomStudent, checkpoint, type, date, otherRoom)
        entityManager.flush()

        val users = repository.findAllByFilters(date, room = room, isCurrentCheckpoint = false)

        assertThat(users.map { it.id }).containsExactly(student.user.id)
    }

    @Test
    fun `과거 출석의 상태 필터도 자퇴생의 기록을 기준으로 적용한다`() {
        val date = today().minusDays(1)
        val attendedStudent = saveStudent(1, isDeleted = true)
        val absentStudent = saveStudent(2, isDeleted = true)
        val checkpoint = saveCheckpoint("저녁")
        val attended = save(AttendanceTypeEntity(name = "출석"))
        val absent = save(AttendanceTypeEntity(name = "외박"))
        saveAttendance(attendedStudent, checkpoint, attended, date)
        saveAttendance(absentStudent, checkpoint, absent, date)
        entityManager.flush()

        val users = repository.findAllByFilters(date, checkpoint, status = attended, isCurrentCheckpoint = false)

        assertThat(users.map { it.id }).containsExactly(attendedStudent.user.id)
    }

    private fun saveStudent(number: Int, isDeleted: Boolean = false): StudentInfoEntity {
        val user = save(UserEntity(
            username = "student-$number", name = "학생$number", role = UserRole.STUDENT, isDeleted = isDeleted
        ))
        return save(StudentInfoEntity(user = user, grade = 1, classNumber = 2, num = number))
    }

    private fun saveCheckpoint(name: String): AttendanceCheckpointEntity = save(AttendanceCheckpointEntity(
        name = name, startAt = LocalTime.of(20, 0), endAt = LocalTime.of(21, 0),
        attendanceStartAt = LocalTime.of(19, 50), attendanceEndAt = LocalTime.of(20, 10)
    ))

    private fun saveAttendance(
        student: StudentInfoEntity,
        checkpoint: AttendanceCheckpointEntity,
        type: AttendanceTypeEntity,
        date: LocalDate,
        room: RoomEntity? = null,
    ): AttendanceEntity = save(AttendanceEntity(
        user = student.user, checkpoint = checkpoint, type = type, date = date, room = room
    ))

    private fun today(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))

    private fun <T : Any> save(entity: T): T {
        if (entity is BaseEntity) {
            entity.createdAt = LocalDateTime.now()
            entity.updatedAt = LocalDateTime.now()
        }
        entityManager.persist(entity)
        return entity
    }
}
