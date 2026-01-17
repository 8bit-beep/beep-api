package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.entity.LimitedUserEntity

data class LimitedUserResponse(
    val id: Long,
    val email: String
) {
    companion object {
        fun of(limitedUser: LimitedUserEntity): LimitedUserResponse {
            return LimitedUserResponse(
                id = limitedUser.id!!,
                email = limitedUser.email
            )
        }
    }
}