package com.b.beep.domain.notification.service

import com.b.beep.domain.notification.controller.dto.request.SaveTokenRequest
import com.b.beep.domain.notification.domain.entity.FcmTokenEntity
import com.b.beep.domain.notification.repository.FcmTokenRepository
import com.b.beep.global.security.ContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val contextHolder: ContextHolder
) {
    @Transactional
    fun saveToken(request: SaveTokenRequest) {
        val user = contextHolder.user

        val existingToken = fcmTokenRepository.findByUser(user)

        if (existingToken != null) {
            existingToken.token = request.token
            existingToken.device = request.device
            fcmTokenRepository.save(existingToken)
        } else {
            try {
                fcmTokenRepository.save(
                    FcmTokenEntity(
                        user = user,
                        token = request.token,
                        device = request.device
                    )
                )
            } catch (e: DataIntegrityViolationException) {
                val token = fcmTokenRepository.findByUser(user)!!
                token.token = request.token
                token.device = request.device
                fcmTokenRepository.save(token)
            }
        }
    }
}
