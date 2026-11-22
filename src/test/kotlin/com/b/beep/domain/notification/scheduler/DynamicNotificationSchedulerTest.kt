package com.b.beep.domain.notification.scheduler

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.notification.service.DiscordWebhookService
import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.room.repository.RoomApprovalRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.support.SimpleTriggerContext
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class DynamicNotificationSchedulerTest {
    private val taskScheduler = mock<TaskScheduler>()
    private val checkpointRepository = mock<AttendanceCheckpointRepository>()
    private val notificationService = mock<NotificationService>()
    private val discordWebhookService = mock<DiscordWebhookService>()
    private val attendanceRepository = mock<AttendanceRepository>()
    private val roomApprovalRepository = mock<RoomApprovalRepository>()
    private val scheduledTasks = mutableListOf<Pair<Runnable, Trigger>>()
    private val scheduler = DynamicNotificationScheduler(
        taskScheduler, checkpointRepository, notificationService,
        discordWebhookService, attendanceRepository, roomApprovalRepository,
    )

    @BeforeEach
    fun setUp() {
        doAnswer { invocation ->
            scheduledTasks += invocation.getArgument<Runnable>(0) to invocation.getArgument<Trigger>(1)
            null
        }.whenever(taskScheduler).schedule(any<Runnable>(), any<Trigger>())
    }

    @Test
    fun `passes checkpoint id and Seoul date to scheduled notifications`() {
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(listOf(checkpoint()))

        scheduler.scheduleAllNotifications()
        val date = LocalDate.now(ZoneId.of("Asia/Seoul"))
        scheduledTasks[0].first.run()
        scheduledTasks[1].first.run()

        verify(notificationService).sendAttendanceStart(1L, date)
        verify(notificationService).sendAttendanceReminder(1L, date)
    }

    @Test
    fun `schedules general notifications on Monday through Thursday in Seoul time`() {
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(listOf(checkpoint()))

        scheduler.scheduleAllNotifications()

        assertThat(nextExecution(0, "2026-11-16T00:00:00Z")).isEqualTo(Instant.parse("2026-11-16T11:00:00Z"))
        assertThat(nextExecution(1, "2026-11-16T00:00:00Z")).isEqualTo(Instant.parse("2026-11-16T11:15:00Z"))
        assertThat(nextExecution(0, "2026-11-17T00:00:00Z")).isEqualTo(Instant.parse("2026-11-17T11:00:00Z"))
        assertThat(nextExecution(0, "2026-11-20T00:00:00Z")).isEqualTo(Instant.parse("2026-11-23T11:00:00Z"))
    }

    @Test
    fun `schedules dedicated notifications only on their configured weekday`() {
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(listOf(checkpoint(DayOfWeek.MONDAY)))

        scheduler.scheduleAllNotifications()

        assertThat(nextExecution(0, "2026-11-17T00:00:00Z")).isEqualTo(Instant.parse("2026-11-23T11:00:00Z"))
        assertThat(nextExecution(1, "2026-11-17T00:00:00Z")).isEqualTo(Instant.parse("2026-11-23T11:15:00Z"))
    }

    @Test
    fun `does not schedule a reminder before the attendance window even near midnight`() {
        val checkpoint = checkpoint().apply {
            attendanceStartAt = LocalTime.MIDNIGHT
            attendanceEndAt = LocalTime.of(0, 3)
        }
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(listOf(checkpoint))

        scheduler.scheduleAllNotifications()
        scheduledTasks[0].first.run()

        assertThat(scheduledTasks).hasSize(2)
        verify(notificationService).sendAttendanceStart(1L, LocalDate.now(ZoneId.of("Asia/Seoul")))
        verifyNoMoreInteractions(notificationService)
    }

    private fun nextExecution(index: Int, instant: String): Instant? =
        scheduledTasks[index].second.nextExecution(
            SimpleTriggerContext(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC))
        )

    private fun checkpoint(dayOfWeek: DayOfWeek? = null) = AttendanceCheckpointEntity(
        id = 1L,
        name = "저녁",
        startAt = LocalTime.of(20, 0),
        endAt = LocalTime.of(22, 0),
        attendanceStartAt = LocalTime.of(20, 0),
        attendanceEndAt = LocalTime.of(20, 20),
        dayOfWeek = dayOfWeek,
        grade = if (dayOfWeek != null) 1 else null,
    )
}
