package com.b.beep.domain.notification.controller

import com.b.beep.domain.notification.controller.dto.request.SaveTokenRequest
import com.b.beep.domain.notification.service.FcmTokenService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/fcm")
class FcmController(
    private val fcmTokenService: FcmTokenService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveFcmToken(@Valid @RequestBody request: SaveTokenRequest) = fcmTokenService.saveToken(request)
}
