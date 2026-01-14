package com.b.beep.domain.notification.service

import com.b.beep.domain.notification.config.FcmMessageFactory
import com.b.beep.domain.notification.controller.dto.request.SendNotificationRequest
import com.b.beep.domain.notification.entity.FcmTokenEntity
import com.b.beep.domain.notification.repository.FcmTokenQueryRepository
import com.b.beep.domain.notification.repository.FcmTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmMessageFactory: FcmMessageFactory,
    private val fcmTokenQueryRepository: FcmTokenQueryRepository
) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendPushMessage(request: SendNotificationRequest, isAll: Boolean) {
        val targetUsers = findTargetUser(isAll)

        targetUsers.forEach { fcmToken ->
            try {
                val message = fcmMessageFactory.createMessage(request, fcmToken.token)
                FirebaseMessaging.getInstance().send(message)
            } catch (e: Exception) {
                log.error("FCM 전송 실패 - user: ${fcmToken.user.email}", e)
            }
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

    private fun findTargetUser(isAll: Boolean): List<FcmTokenEntity> {
        if (isAll) return fcmTokenRepository.findAll()
        return fcmTokenQueryRepository.findAllByNotAttendStatus()
    }
}
