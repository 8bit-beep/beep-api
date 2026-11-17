package com.b.beep.domain.notification.scheduler

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.notification.service.DiscordWebhookService
import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.room.repository.RoomApprovalRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.ScheduledFuture

@Component
class DynamicNotificationScheduler(
    private val taskScheduler: TaskScheduler,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val notificationService: NotificationService,
    private val discordWebhookService: DiscordWebhookService,
    private val attendanceRepository: AttendanceRepository,
    private val roomApprovalRepository: RoomApprovalRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scheduledTasks = mutableListOf<ScheduledFuture<*>>()

    @PostConstruct
    fun init() {
        scheduleAllNotifications()
    }

    fun scheduleAllNotifications() {
        cancelAll()
        val checkpoints = checkpointRepository.findAllByIsDeletedFalse()

        checkpoints.forEach { checkpoint ->
            val checkpointId = checkpoint.id ?: return@forEach
            val attendanceStart = checkpoint.attendanceStartAt

            scheduleDaily(attendanceStart, "${checkpoint.name} 출석 시작", checkpoint.dayOfWeek, SEOUL_ZONE) {
                logger.info("${checkpoint.name} 출석 시작 알림 전송")
                notificationService.sendAttendanceStart(checkpointId, LocalDate.now(SEOUL_ZONE))
            }

            val attendanceEnd = checkpoint.attendanceEndAt
            if (Duration.between(attendanceStart, attendanceEnd).toMinutes() < 5) return@forEach

            val reminderTime = attendanceEnd.minusMinutes(5)
            scheduleDaily(reminderTime, "${checkpoint.name} 마감 5분 전", checkpoint.dayOfWeek, SEOUL_ZONE) {
                logger.info("${checkpoint.name} 마감 임박 알림 전송")
                notificationService.sendAttendanceReminder(checkpointId, LocalDate.now(SEOUL_ZONE))
            }
        }

        checkpoints.forEach { checkpoint ->
            val endAt = checkpoint.endAt
            val approvalReminderTime = endAt.minusMinutes(5)
            scheduleDaily(approvalReminderTime, "${checkpoint.name} 미승인 실 알림") {
                logger.info("${checkpoint.name} 미승인 실 확인 중")
                val today = LocalDate.now()
                val roomsWithAttendance = attendanceRepository.findDistinctRoomsByCheckpointAndDate(checkpoint, today)
                val approvedRoomIds = roomApprovalRepository.findApprovedRoomIdsByCheckpointAndDate(checkpoint, today)
                val unapprovedRooms = roomsWithAttendance.filter { it.id !in approvedRoomIds }

                if (unapprovedRooms.isNotEmpty()) {
                    val roomNames = unapprovedRooms.joinToString(", ") { it.name }
                    discordWebhookService.send(
                        "미승인실 : [$roomNames]\nhttps://docs.google.com/spreadsheets/d/1NBGqXzb-VrFiZUu0y4t7qIWeg69HQjDFgHN8RMOQr8s"
                    )
                }
            }
        }

        logger.info("동적 알림 스케줄 등록 완료: ${scheduledTasks.size}개")
    }

    private fun scheduleDaily(
        time: LocalTime,
        description: String,
        dayOfWeek: DayOfWeek? = null,
        zoneId: ZoneId? = null,
        task: Runnable,
    ) {
        val days = dayOfWeek?.name?.take(3) ?: "MON-THU"
        val cronExpression = "0 ${time.minute} ${time.hour} * * $days"
        val trigger = if (zoneId != null) CronTrigger(cronExpression, zoneId) else CronTrigger(cronExpression)
        val scheduledTask = taskScheduler.schedule(task, trigger)
        scheduledTask?.let {
            scheduledTasks.add(it)
            logger.info("스케줄 등록: $description ($cronExpression)")
        }
    }

    private fun cancelAll() {
        scheduledTasks.forEach { it.cancel(false) }
        scheduledTasks.clear()
        logger.info("기존 스케줄 전체 취소")
    }

    companion object {
        private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")
    }
}
