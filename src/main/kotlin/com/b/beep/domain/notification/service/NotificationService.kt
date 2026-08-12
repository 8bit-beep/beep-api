package com.b.beep.domain.notification.service

import com.b.beep.domain.notification.controller.dto.request.SendNotificationRequest
import com.b.beep.domain.notification.infrastructure.DodamNotificationClient
import com.b.beep.domain.notification.repository.NotificationTargetQueryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val dodamNotificationClient: DodamNotificationClient,
    private val notificationTargetQueryRepository: NotificationTargetQueryRepository,
) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendPushMessage(request: SendNotificationRequest, isAll: Boolean) {
        val targetUserPublicIds = findTargetUserPublicIds(isAll)
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
}
