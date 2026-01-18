package com.b.beep.domain.user.repository

import com.b.beep.domain.user.domain.entity.LimitedUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LimitedUserRepository : JpaRepository<LimitedUserEntity, Long> {
    fun existsByEmail(email: String): Boolean
}
