package com.b.beep.domain.notification.repository

import com.b.beep.domain.notification.entity.FcmTokenEntity
import com.b.beep.domain.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmTokenEntity, Long> {
    fun findByUserAndDevice(user: UserEntity, device: String): FcmTokenEntity?
    fun findByUser(user: UserEntity): FcmTokenEntity?
}
