package com.b.beep.domain.user.service

import com.b.beep.domain.user.controller.dto.request.CreateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.request.UpdateLimitedUserRequest
import com.b.beep.domain.user.domain.entity.LimitedUserEntity
import com.b.beep.domain.user.error.LimitedUserError
import com.b.beep.domain.user.repository.LimitedUserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LimitedUserService(
    private val limitedUserRepository: LimitedUserRepository
) {
    fun createLimitedUser(request: CreateLimitedUserRequest) {
        if (limitedUserRepository.existsByEmail(request.email)) {
            throw CustomException(LimitedUserError.ALREADY_EXIST_LIMITED_USER)
        }
        limitedUserRepository.save(
            LimitedUserEntity(email = request.email)
        )
    }

    @Transactional(readOnly = true)
    fun getAll(): List<LimitedUserEntity> {
        return limitedUserRepository.findAll()
    }

    fun updateLimitedUser(limitedUserId: Long, request: UpdateLimitedUserRequest) {
        val limitedUser = getLimitedUserOrThrow(limitedUserId)

        if (limitedUser.email != request.email && limitedUserRepository.existsByEmail(request.email)) {
            throw CustomException(LimitedUserError.ALREADY_EXIST_LIMITED_USER)
        }
        limitedUser.email = request.email
    }

    fun deleteLimitedUser(limitedUserId: Long) {
        val limitedUser = getLimitedUserOrThrow(limitedUserId)
        limitedUserRepository.delete(limitedUser)
    }

    private fun getLimitedUserOrThrow(limitedUserId: Long): LimitedUserEntity {
        return limitedUserRepository.findByIdOrNull(limitedUserId)
            ?: throw CustomException(LimitedUserError.LIMITED_USER_NOT_FOUND)
    }
}