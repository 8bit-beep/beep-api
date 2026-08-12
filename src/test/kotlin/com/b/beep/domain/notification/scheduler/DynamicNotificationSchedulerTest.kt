package com.b.beep.domain.notification.scheduler

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.notification.service.DiscordWebhookService
import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.room.repository.RoomApprovalRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import java.time.LocalTime

class DynamicNotificationSchedulerTest {
    private val taskScheduler = mock<TaskScheduler>()
    private val checkpointRepository = mock<AttendanceCheckpointRepository>()
    private val notificationService = mock<NotificationService>()
    private val discordWebhookService = mock<DiscordWebhookService>()
    private val attendanceRepository = mock<AttendanceRepository>()
    private val roomApprovalRepository = mock<RoomApprovalRepository>()

    @Test
    fun `keeps attendance start and five minute deadline notification contents`() {
        val scheduledTasks = mutableListOf<Runnable>()
        doAnswer { invocation ->
            scheduledTasks += invocation.getArgument<Runnable>(0)
            null
        }.whenever(taskScheduler).schedule(any<Runnable>(), any<Trigger>())
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(
            listOf(
                AttendanceCheckpointEntity(
                    name = "저녁",
                    startAt = LocalTime.of(20, 0),
                    endAt = LocalTime.of(22, 0),
                    attendanceStartAt = LocalTime.of(20, 0),
                    attendanceEndAt = LocalTime.of(21, 0),
                )
            )
        )
        val scheduler = DynamicNotificationScheduler(
            taskScheduler = taskScheduler,
            checkpointRepository = checkpointRepository,
            notificationService = notificationService,
            discordWebhookService = discordWebhookService,
            attendanceRepository = attendanceRepository,
            roomApprovalRepository = roomApprovalRepository,
        )

        scheduler.scheduleAllNotifications()
        scheduledTasks[0].run()
        scheduledTasks[1].run()

        verify(notificationService).sendToAll(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png",
        )
        verify(notificationService).sendToNotAttended(
            title = "출석 마감 임박!",
            body = "저녁 출석이 5분 후 마감됩니다.",
            imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png",
        )
    }
}
