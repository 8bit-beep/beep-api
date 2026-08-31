package com.b.beep.domain.attendance.service

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("교사 출석 상태 변경")
class TeacherAttendanceServiceTest {

    @Mock private lateinit var studentInfoRepository: StudentInfoRepository
    @Mock private lateinit var attendanceRepository: AttendanceRepository
    @Mock private lateinit var checkpointResolver: CheckpointResolver
    @Mock private lateinit var checkpointRepository: AttendanceCheckpointRepository
    @Mock private lateinit var attendanceQueryRepository: AttendanceQueryRepository
    @Mock private lateinit var roomRepository: RoomRepository
    @Mock private lateinit var attendanceTypeService: AttendanceTypeService
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var attendancePlacementService: AttendancePlacementService
    @Mock private lateinit var roomCheckpointResolver: RoomCheckpointResolver

    @InjectMocks private lateinit var service: TeacherAttendanceService

    private val date: LocalDate = LocalDate.of(2026, 8, 26)
    private val student = UserEntity(id = 11L, username = "s11", name = "김철수", role = UserRole.STUDENT)
    private val teacher = UserEntity(id = 500L, username = "t", name = "천준범", role = UserRole.TEACHER)
    private val notAttended =
        AttendanceTypeEntity(id = 3L, name = AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME)
    private val attended = AttendanceTypeEntity(id = 1L, name = "출석")
    private val studentInfo = StudentInfoEntity(
        id = 1L,
        user = student,
        grade = 1,
        classNumber = 2,
        num = 3
    )
    private val checkpoint = AttendanceCheckpointEntity(
        id = 1L, name = "8~9교시",
        startAt = LocalTime.of(16, 30), endAt = LocalTime.of(18, 59),
        attendanceStartAt = LocalTime.of(14, 0), attendanceEndAt = LocalTime.of(18, 39)
    )

    private fun attendance(
        absence: AbsenceEntity? = null,
        event: EventEntity? = null
    ) = AttendanceEntity(
        id = 1L,
        checkpoint = checkpoint,
        type = AttendanceTypeEntity(id = 9L, name = "기타"),
        user = student,
        date = date,
        absence = absence,
        event = event
    )

    private fun revertToNotAttended(existing: AttendanceEntity) {
        `when`(userRepository.findByIdAndIsDeletedFalse(11L)).thenReturn(student)
        `when`(attendanceTypeService.getAttendanceTypeEntityById(3L)).thenReturn(notAttended)
        `when`(checkpointRepository.findById(1L)).thenReturn(java.util.Optional.of(checkpoint))
        `when`(attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, student, date))
            .thenReturn(existing)

        service.updateStudentStatus(
            UpdateStatusRequest(userId = 11L, statusId = 3L, date = date, checkpointId = 1L)
        )
    }

    @Test
    @DisplayName("체크포인트가 없으면 학생 학년에 맞는 현재 교시에 저장한다")
    fun usesStudentGradeToResolveCurrentCheckpoint() {
        `when`(userRepository.findByIdAndIsDeletedFalse(11L)).thenReturn(student)
        `when`(attendanceTypeService.getAttendanceTypeEntityById(1L)).thenReturn(attended)
        `when`(studentInfoRepository.findByUser(student)).thenReturn(studentInfo)
        `when`(roomCheckpointResolver.getCurrentCheckpointOrNearest(date, null, 1)).thenReturn(checkpoint)

        service.updateStudentStatus(
            UpdateStatusRequest(userId = 11L, statusId = 1L, date = date)
        )

        verify(roomCheckpointResolver).getCurrentCheckpointOrNearest(date, null, 1)
        verify(checkpointResolver, never()).getCurrentCheckpointOrNearest()
        verify(attendanceRepository).findByCheckpointAndUserAndDate(checkpoint, student, date)
    }

    @Nested
    @DisplayName("미출석으로 되돌릴 때")
    inner class RevertToNotAttended {

        @Test
        @DisplayName("행사에서 파생된 출석은 지우지 않는다")
        fun keepsEventDerivedAttendance() {
            val derived = attendance(
                event = EventEntity(id = 7L, name = "체육대회", date = date, createdBy = teacher)
            )

            revertToNotAttended(derived)

            verify(attendanceRepository, never()).delete(derived)
        }

        @Test
        @DisplayName("외박에서 파생된 출석은 지우지 않는다")
        fun keepsAbsenceDerivedAttendance() {
            val derived = attendance(
                absence = AbsenceEntity(
                    id = 5L, startDate = date, endDate = date, reason = "외박",
                    type = AttendanceTypeEntity(id = 4L, name = "외박")
                )
            )

            revertToNotAttended(derived)

            verify(attendanceRepository, never()).delete(derived)
        }

        @Test
        @DisplayName("학생이 직접 찍은 출석은 지운다")
        fun deletesSelfMadeAttendance() {
            val own = attendance()

            revertToNotAttended(own)

            verify(attendanceRepository).delete(own)
        }
    }
}
