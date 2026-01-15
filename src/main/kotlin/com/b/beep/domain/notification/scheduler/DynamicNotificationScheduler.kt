package com.b.beep.domain.notification.scheduler

import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.period.repository.PeriodRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.util.concurrent.ScheduledFuture

@Component
class DynamicNotificationScheduler(
    private val taskScheduler: TaskScheduler,
    private val periodRepository: PeriodRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scheduledTasks = mutableListOf<ScheduledFuture<*>>()

    @PostConstruct
    fun init() {
        scheduleAllNotifications()
    }

    fun scheduleAllNotifications() {
        cancelAll()
        val periods = periodRepository.findAll()

        periods.forEach { period ->
            scheduleDaily(period.attendStartTime, "${period.period}교시 출석 시작") {
                logger.info("${period.period}교시 출석 시작 알림 전송")
                notificationService.sendToAll(
                    title = "출석 시간입니다!",
                    body = "${period.period}교시 출석이 시작되었습니다.",
                    imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
                )
            }

            val reminderTime = period.attendEndTime.minusMinutes(5)
            scheduleDaily(reminderTime, "${period.period}교시 마감 5분 전") {
                logger.info("${period.period}교시 마감 임박 알림 전송")
                notificationService.sendToNotAttended(
                    title = "출석 마감 임박!",
                    body = "${period.period}교시 출석이 5분 후 마감됩니다.",
                    imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
                )
            }
        }

        logger.info("동적 알림 스케줄 등록 완료: ${scheduledTasks.size}개")
    }

    private fun scheduleDaily(time: LocalTime, description: String, task: Runnable) {
        val cronExpression = "0 ${time.minute} ${time.hour} * * MON-FRI"
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
