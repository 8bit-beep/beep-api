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
import java.time.LocalDate
import java.time.LocalTime
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
            val attendanceStart = checkpoint.attendanceStartAt ?: return@forEach

            scheduleDaily(attendanceStart, "${checkpoint.name} 출석 시작") {
                logger.info("${checkpoint.name} 출석 시작 알림 전송")
                notificationService.sendToAll(
                    title = "출석 시간입니다!",
                    body = "${checkpoint.name} 출석이 시작되었습니다.",
                    imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
                )
            }

            val attendanceEnd = checkpoint.attendanceEndAt ?: return@forEach
            val reminderTime = attendanceEnd.minusMinutes(5)
            scheduleDaily(reminderTime, "${checkpoint.name} 마감 5분 전") {
                logger.info("${checkpoint.name} 마감 임박 알림 전송")
                notificationService.sendToNotAttended(
                    title = "출석 마감 임박!",
                    body = "${checkpoint.name} 출석이 5분 후 마감됩니다.",
                    imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
                )
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

    private fun scheduleDaily(time: LocalTime, description: String, task: Runnable) {
        val cronExpression = "0 ${time.minute} ${time.hour} * * MON-THU"
        val trigger = CronTrigger(cronExpression)
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
}
