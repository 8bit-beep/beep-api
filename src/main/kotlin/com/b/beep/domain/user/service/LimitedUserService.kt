package com.b.beep.domain.user.service

import com.b.beep.domain.user.controller.dto.request.CreateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.request.UpdateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.response.LimitedUserResponse
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
        validateUsernameNotExists(request.username)
        limitedUserRepository.save(
            LimitedUserEntity(username = request.username)
        )
    }

    @Transactional(readOnly = true)
    fun getLimitedUsers(): List<LimitedUserResponse> {
        return limitedUserRepository.findAll().map { LimitedUserResponse.of(it) }
    }

    fun updateLimitedUser(limitedUserId: Long, request: UpdateLimitedUserRequest) {
        val limitedUser = getLimitedUserEntityOrThrow(limitedUserId)

        if (limitedUser.username != request.username) {
            validateUsernameNotExists(request.username)
        }
        limitedUser.username = request.username
    }

    fun deleteLimitedUser(limitedUserId: Long) {
        val limitedUser = getLimitedUserEntityOrThrow(limitedUserId)
        limitedUserRepository.delete(limitedUser)
    }

    private fun getLimitedUserEntityOrThrow(limitedUserId: Long): LimitedUserEntity {
        return limitedUserRepository.findByIdOrNull(limitedUserId)
            ?: throw CustomException(LimitedUserError.LIMITED_USER_NOT_FOUND)
    }

    private fun validateUsernameNotExists(username: String) {
        if (limitedUserRepository.existsByUsername(username)) {
            throw CustomException(LimitedUserError.ALREADY_EXIST_LIMITED_USER)
        }
    }
}
