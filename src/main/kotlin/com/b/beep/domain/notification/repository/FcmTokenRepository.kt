package com.b.beep.domain.notification.repository

import com.b.beep.domain.notification.domain.entity.FcmTokenEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmTokenEntity, Long> {
    fun findByUser(user: UserEntity): FcmTokenEntity?
}
