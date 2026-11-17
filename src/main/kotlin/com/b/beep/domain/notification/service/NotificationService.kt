package com.b.beep.domain.notification.service

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.notification.controller.dto.request.SendNotificationRequest
import com.b.beep.domain.notification.infrastructure.DodamNotificationClient
import com.b.beep.domain.notification.repository.NotificationTargetQueryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate

@Service
class NotificationService(
    private val dodamNotificationClient: DodamNotificationClient,
    private val notificationTargetQueryRepository: NotificationTargetQueryRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val checkpointResolver: CheckpointResolver,
) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendPushMessage(request: SendNotificationRequest, isAll: Boolean) {
        val targetUserPublicIds = findTargetUserPublicIds(isAll)
        sendPushMessage(request, targetUserPublicIds)
    }

    fun sendAttendanceStart(checkpointId: Long, date: LocalDate) {
        val checkpoint = checkpointRepository.findByIdAndIsDeletedFalse(checkpointId) ?: return
        val grades = findTargetGrades(checkpointId, date)
        if (grades.isEmpty()) return

        val durationMinutes = Duration.between(checkpoint.attendanceStartAt, checkpoint.attendanceEndAt).toMinutes()
        val request = SendNotificationRequest(
            title = "출석 시간입니다!",
            body = "지금부터 ${durationMinutes}분 간 출석이 가능합니다.",
        )
        sendPushMessage(request, notificationTargetQueryRepository.findAllActiveStudentPublicIds(grades))
    }

    fun sendAttendanceReminder(checkpointId: Long, date: LocalDate) {
        val checkpoint = checkpointRepository.findByIdAndIsDeletedFalse(checkpointId) ?: return
        val grades = findTargetGrades(checkpointId, date)
        if (grades.isEmpty()) return

        val request = SendNotificationRequest(
            title = "출석 마감 임박!",
            body = "${checkpoint.name} 출석이 5분 후 마감됩니다.",
        )
        sendPushMessage(
            request,
            notificationTargetQueryRepository.findAllNotAttendedPublicIds(checkpointId, date, grades),
        )
    }

    private fun findTargetGrades(checkpointId: Long, date: LocalDate): List<Int> =
        TARGET_GRADES.filter { grade ->
            // 출석 처리와 동일하게 전용 구간과 겹치는 일반 구간을 제외합니다.
            checkpointResolver.getCheckpointsForStudent(grade, date.dayOfWeek).any { it.id == checkpointId }
        }

    private fun sendPushMessage(request: SendNotificationRequest, targetUserPublicIds: List<String>) {
        if (targetUserPublicIds.isEmpty()) {
            log.info("도담 알림 발송을 건너뜁니다: 발송 대상자가 없습니다.")
            return
        }

        try {
            val result = dodamNotificationClient.send(
                title = request.title,
                body = request.body,
                targetUserPublicIds = targetUserPublicIds,
            ) ?: return

            if (result.failureCount > 0) {
                log.warn(
                    "도담 알림 발송이 일부 실패했습니다: 대상자 수={}, 성공={}, 실패={}",
                    targetUserPublicIds.size,
                    result.successCount,
                    result.failureCount,
                )
            } else {
                log.info(
                    "도담 알림을 발송했습니다: 대상자 수={}, 성공={}",
                    targetUserPublicIds.size,
                    result.successCount,
                )
            }
        } catch (exception: Exception) {
            log.error(
                "도담 알림 발송 요청에 실패했습니다: 대상자 수={}, 예외 유형={}, 오류 메시지={}",
                targetUserPublicIds.size,
                exception.javaClass.simpleName,
                exception.message,
            )
        }
    }

    fun sendToAll(title: String, body: String, imageUrl: String) {
        val request = SendNotificationRequest(title, body, imageUrl)
        sendPushMessage(request, true)
    }

    fun sendToNotAttended(title: String, body: String, imageUrl: String) {
        val request = SendNotificationRequest(title, body, imageUrl)
        sendPushMessage(request, false)
    }

    private fun findTargetUserPublicIds(isAll: Boolean): List<String> =
        if (isAll) {
            notificationTargetQueryRepository.findAllActiveStudentPublicIds()
        } else {
            notificationTargetQueryRepository.findAllNotAttendedPublicIds()
        }

    companion object {
        private val TARGET_GRADES = listOf(1, 2)
    }
}
