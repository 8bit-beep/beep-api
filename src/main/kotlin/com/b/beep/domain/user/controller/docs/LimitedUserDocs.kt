package com.b.beep.domain.user.controller.docs

import com.b.beep.domain.user.controller.dto.request.CreateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.request.UpdateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.response.LimitedUserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "제한 사용자", description = "블랙리스트 계정 관리 API")
interface LimitedUserDocs {
    @Operation(summary = "제한 사용자 추가", description = "블랙리스트에 추가할 계정을 등록합니다.")
    fun createLimitedUser(@Valid request: CreateLimitedUserRequest)

    @Operation(summary = "제한 사용자 목록 조회", description = "블랙리스트에 추가된 계정 목록을 조회합니다.")
    fun getLimitedUsers() : List<LimitedUserResponse>

    @Operation(summary = "제한 사용자 수정", description = "블랙리스트 계정을 수정합니다.")
    fun updateLimitedUser(@Positive(message = "ID는 양수여야 합니다") limitedUserId: Long, @Valid request: UpdateLimitedUserRequest)

    @Operation(summary = "제한 사용자 삭제", description = "블랙리스트에 있는 계정을 삭제합니다.")
    fun deleteLimitedUser(@Positive(message = "ID는 양수여야 합니다") limitedUserId: Long)
}
