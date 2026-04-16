package com.b.beep.global.security

import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class ContextHolder(
    private val userRepository: UserRepository,
) {
    val user: UserEntity
        get() {
            val subject = SecurityContextHolder.getContext().authentication.name
            return userRepository.findByUsernameAndIsDeletedFalse(subject)
                ?: userRepository.findByPublicIdAndIsDeletedFalse(subject)
                ?: throw CustomException(UserError.USER_NOT_FOUND)
        }
}