package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.response.AbsenceSource
import com.b.beep.domain.absence.domain.AbsenceValidator
import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import com.b.beep.domain.absence.repository.AbsenceExceptionRepository
import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.absence.repository.AbsenceUserRepository
import com.b.beep.domain.absence.repository.OutSleepingQueryRepository
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class AbsenceServiceTest {
    @Mock
    private lateinit var absenceRepository: AbsenceRepository

    @Mock
    private lateinit var absenceUserRepository: AbsenceUserRepository

    @Mock
    private lateinit var absenceExceptionRepository: AbsenceExceptionRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var studentInfoRepository: StudentInfoRepository

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    @Mock
    private lateinit var absenceValidator: AbsenceValidator

    @Mock
    private lateinit var attendanceTypeService: AttendanceTypeService

    @Mock
    private lateinit var outSleepingQueryRepository: OutSleepingQueryRepository

    @InjectMocks
    private lateinit var service: AbsenceService

    @Test
    fun `오늘 외박 조회에 등록 외박과 수동 외박을 합쳐 페이징한다`() {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val outSleepingType = AttendanceTypeEntity(
            id = 4L,
            name = AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME,
        )
        val registeredStudent = student(1L, "registered", "등록 학생")
        val manuallyChangedStudent = student(2L, "manual", "수동 학생")
        val registeredAbsence = AbsenceEntity(
            id = 10L,
            startDate = today,
            endDate = today,
            reason = "등록 외박",
            type = outSleepingType,
        )

        whenever(absenceRepository.findAllByDateAndIsDeletedFalse(today, Pageable.unpaged()))
            .thenReturn(PageImpl(listOf(registeredAbsence)))
        whenever(absenceUserRepository.findAllByAbsenceId(10L))
            .thenReturn(listOf(AbsenceUserEntity(user = registeredStudent, absence = registeredAbsence)))
        whenever(absenceExceptionRepository.findAllByAbsenceId(10L)).thenReturn(emptyList())
        whenever(studentInfoRepository.findByUser(registeredStudent))
            .thenReturn(studentInfo(registeredStudent, grade = 1))
        whenever(studentInfoRepository.findByUser(manuallyChangedStudent))
            .thenReturn(studentInfo(manuallyChangedStudent, grade = 2))
        whenever(outSleepingQueryRepository.findAllManuallyChangedUsers(today))
            .thenReturn(listOf(manuallyChangedStudent))
        whenever(attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME))
            .thenReturn(outSleepingType)

        val firstPage = service.getAbsencesToday(PageRequest.of(0, 1))
        val secondPage = service.getAbsencesToday(PageRequest.of(1, 1))

        assertThat(firstPage.totalElements).isEqualTo(2)
        val registeredResponse = firstPage.content.single()
        assertThat(registeredResponse.absenceId).isEqualTo(10L)
        assertThat(registeredResponse.source).isEqualTo(AbsenceSource.ABSENCE)
        assertThat(registeredResponse.reason).isEqualTo("등록 외박")

        assertThat(secondPage.totalElements).isEqualTo(2)
        val manuallyChangedResponse = secondPage.content.single()
        assertThat(manuallyChangedResponse.absenceId).isNull()
        assertThat(manuallyChangedResponse.source).isEqualTo(AbsenceSource.ATTENDANCE)
        assertThat(manuallyChangedResponse.reason).isEqualTo("외박")
        assertThat(manuallyChangedResponse.startDate).isEqualTo(today)
        assertThat(manuallyChangedResponse.endDate).isEqualTo(today)
        assertThat(manuallyChangedResponse.checkpoints).isEmpty()
        assertThat(manuallyChangedResponse.typeId).isEqualTo(4L)
        assertThat(manuallyChangedResponse.typeName).isEqualTo(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME)
        assertThat(manuallyChangedResponse.targetStudents.single().username).isEqualTo("manual")
    }

    @Test
    fun `수동 외박 학생이 없으면 외박 출석 타입을 조회하지 않는다`() {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        whenever(absenceRepository.findAllByDateAndIsDeletedFalse(today, Pageable.unpaged()))
            .thenReturn(PageImpl(emptyList()))
        whenever(outSleepingQueryRepository.findAllManuallyChangedUsers(today)).thenReturn(emptyList())

        val result = service.getAbsencesToday(Pageable.unpaged())

        assertThat(result.content).isEmpty()
        verify(attendanceTypeService, never())
            .getAttendanceTypeEntityByName(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME)
    }

    private fun student(id: Long, username: String, name: String) = UserEntity(
        id = id,
        username = username,
        name = name,
        role = UserRole.STUDENT,
    )

    private fun studentInfo(user: UserEntity, grade: Int) = StudentInfoEntity(
        user = user,
        grade = grade,
        classNumber = 1,
        num = 1,
    )
}
