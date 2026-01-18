package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.user.domain.entity.LimitedUserEntity

data class LimitedUserResponse(
    val id: Long,
    val email: String
) {
    companion object {
        fun of(entity: LimitedUserEntity): LimitedUserResponse {
            return LimitedUserResponse(
                id = entity.id!!,
                email = entity.email
            )
        }
    }
}