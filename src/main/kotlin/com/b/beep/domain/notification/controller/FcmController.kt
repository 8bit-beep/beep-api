package com.b.beep.domain.notification.controller

import com.b.beep.domain.notification.controller.dto.request.SaveTokenRequest
import com.b.beep.domain.notification.service.FcmTokenService
import com.b.beep.domain.notification.service.NotificationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/fcm")
class FcmController(
    private val fcmTokenService: FcmTokenService,
    private val notificationService: NotificationService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveFcmToken(@Valid @RequestBody request: SaveTokenRequest) = fcmTokenService.saveToken(request)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun testPushNotification() {
        notificationService.sendToAll(
            title = "출석 시간입니다! 출석하세요",
            body = "지금부터 20분 간 출석이 가능합니다.",
            imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
        )
    }
}
