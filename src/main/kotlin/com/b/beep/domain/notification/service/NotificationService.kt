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
            log.info("Dodam notification skipped: no eligible targets")
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
                    "Dodam notification completed with failures: targets={}, success={}, failure={}",
                    targetUserPublicIds.size,
                    result.successCount,
                    result.failureCount,
                )
            } else {
                log.info(
                    "Dodam notification sent: targets={}, success={}",
                    targetUserPublicIds.size,
                    result.successCount,
                )
            }
        } catch (exception: Exception) {
            log.error(
                "Dodam notification request failed: targets={}, exceptionType={}, message={}",
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
