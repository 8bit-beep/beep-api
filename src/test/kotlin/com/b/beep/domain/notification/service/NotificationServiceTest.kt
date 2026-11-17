package com.b.beep.domain.notification.service

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.notification.controller.dto.request.SendNotificationRequest
import com.b.beep.domain.notification.infrastructure.DodamNotificationClient
import com.b.beep.domain.notification.infrastructure.DodamNotificationResult
import com.b.beep.domain.notification.repository.NotificationTargetQueryRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class NotificationServiceTest {
    private val client = mock<DodamNotificationClient>()
    private val targetRepository = mock<NotificationTargetQueryRepository>()
    private val checkpointRepository = mock<AttendanceCheckpointRepository>()
    private val checkpoints = listOf(
        checkpoint(1, "8~9교시", "16:30", "18:59"),
        checkpoint(2, "10~11교시", "19:00", "20:39"),
        checkpoint(3, "최종 출석", "20:40", "21:50"),
        checkpoint(4, "7~8교시", "15:20", "17:19", DayOfWeek.MONDAY, 1),
        checkpoint(5, "9교시", "17:20", "18:10", DayOfWeek.MONDAY, 1),
    )
    private lateinit var service: NotificationService

    @BeforeEach
    fun setUp() {
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(checkpoints)
        whenever(checkpointRepository.findByIdAndIsDeletedFalse(any())).thenAnswer { invocation ->
            checkpoints.find { it.id == invocation.getArgument<Long>(0) }
        }
        service = NotificationService(client, targetRepository, checkpointRepository, CheckpointResolver(checkpointRepository))
    }

    @Test
    fun `sends attendance start notification to all eligible students through Dodam`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(listOf("student-1", "student-2"))
        whenever(client.send(any(), any(), any())).thenReturn(DodamNotificationResult(2, 0))

        service.sendToAll(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            imageUrl = "legacy-image",
        )

        verify(client).send(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            targetUserPublicIds = listOf("student-1", "student-2"),
        )
    }

    @Test
    fun `sends attendance deadline notification only to not attended students`() {
        whenever(targetRepository.findAllNotAttendedPublicIds()).thenReturn(listOf("student-2"))
        whenever(client.send(any(), any(), any())).thenReturn(DodamNotificationResult(1, 0))

        service.sendToNotAttended(
            title = "출석 마감 임박!",
            body = "저녁 출석이 5분 후 마감됩니다.",
            imageUrl = "legacy-image",
        )

        verify(client).send(
            title = "출석 마감 임박!",
            body = "저녁 출석이 5분 후 마감됩니다.",
            targetUserPublicIds = listOf("student-2"),
        )
    }

    @Test
    fun `does not call Dodam API when there are no targets`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(emptyList())

        service.sendPushMessage(SendNotificationRequest("title", "body"), isAll = true)

        verify(client, never()).send(any(), any(), any())
    }

    @Test
    fun `does not propagate Dodam API failures`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(listOf("student-1"))
        whenever(client.send(any(), any(), any())).thenThrow(IllegalStateException("Dodam unavailable"))

        assertDoesNotThrow {
            service.sendPushMessage(SendNotificationRequest("title", "body"), isAll = true)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["2026-11-16", "2026-11-17", "2026-11-18", "2026-11-19"])
    fun `scopes start and reminder notifications to effective checkpoints for each grade`(dateText: String) {
        val date = LocalDate.parse(dateText)
        whenever(targetRepository.findAllActiveStudentPublicIds(any())).thenReturn(listOf("student"))
        whenever(targetRepository.findAllNotAttendedPublicIds(any(), any(), any())).thenReturn(listOf("missing"))
        val expectedGrades = if (date.dayOfWeek == DayOfWeek.MONDAY) {
            mapOf(1L to listOf(2), 2L to listOf(1, 2), 3L to listOf(1, 2), 4L to listOf(1), 5L to listOf(1))
        } else {
            mapOf(1L to listOf(1, 2), 2L to listOf(1, 2), 3L to listOf(1, 2))
        }

        checkpoints.forEach { checkpoint ->
            clearInvocations(targetRepository, client)
            val checkpointId = checkpoint.id!!

            service.sendAttendanceStart(checkpointId, date)
            service.sendAttendanceReminder(checkpointId, date)

            val grades = expectedGrades[checkpointId]
            if (grades == null) {
                verifyNoInteractions(targetRepository, client)
            } else {
                verify(targetRepository).findAllActiveStudentPublicIds(grades)
                verify(targetRepository).findAllNotAttendedPublicIds(checkpointId, date, grades)
                verify(client).send("출석 시간입니다!", "지금부터 20분 간 출석이 가능합니다.", listOf("student"))
                verify(client).send("출석 마감 임박!", "${checkpoint.name} 출석이 5분 후 마감됩니다.", listOf("missing"))
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 20, 30])
    fun `calculates attendance duration from checkpoint settings`(minutes: Int) {
        val checkpoint = checkpoints.first { it.id == 2L }
        checkpoint.attendanceEndAt = checkpoint.attendanceStartAt.plusMinutes(minutes.toLong())
        whenever(targetRepository.findAllActiveStudentPublicIds(listOf(1, 2))).thenReturn(listOf("student"))

        service.sendAttendanceStart(2L, LocalDate.of(2026, 11, 16))

        verify(client).send("출석 시간입니다!", "지금부터 ${minutes}분 간 출석이 가능합니다.", listOf("student"))
    }

    @Test
    fun `skips notifications when checkpoint is missing or deleted`() {
        val date = LocalDate.of(2026, 11, 16)

        service.sendAttendanceStart(99L, date)
        service.sendAttendanceReminder(99L, date)

        verifyNoInteractions(targetRepository, client)
    }

    @Test
    fun `skips scheduled notifications when no eligible students remain`() {
        val date = LocalDate.of(2026, 11, 16)
        whenever(targetRepository.findAllActiveStudentPublicIds(listOf(1, 2))).thenReturn(emptyList())
        whenever(targetRepository.findAllNotAttendedPublicIds(2L, date, listOf(1, 2))).thenReturn(emptyList())

        service.sendAttendanceStart(2L, date)
        service.sendAttendanceReminder(2L, date)

        verifyNoInteractions(client)
    }

    @Test
    fun `does not propagate scheduled notification failures`() {
        val date = LocalDate.of(2026, 11, 16)
        whenever(targetRepository.findAllActiveStudentPublicIds(listOf(1, 2))).thenReturn(listOf("student"))
        whenever(targetRepository.findAllNotAttendedPublicIds(2L, date, listOf(1, 2))).thenReturn(listOf("missing"))
        whenever(client.send(any(), any(), any())).thenThrow(IllegalStateException("Dodam unavailable"))

        assertDoesNotThrow { service.sendAttendanceStart(2L, date) }
        assertDoesNotThrow { service.sendAttendanceReminder(2L, date) }
    }

    private fun checkpoint(
        id: Long,
        name: String,
        start: String,
        end: String,
        dayOfWeek: DayOfWeek? = null,
        grade: Int? = null,
    ): AttendanceCheckpointEntity {
        val startAt = LocalTime.parse(start)
        return AttendanceCheckpointEntity(
            id = id,
            name = name,
            startAt = startAt,
            endAt = LocalTime.parse(end),
            attendanceStartAt = startAt,
            attendanceEndAt = startAt.plusMinutes(20),
            dayOfWeek = dayOfWeek,
            grade = grade,
        )
    }
}
