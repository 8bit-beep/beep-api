package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.dto.request.CreateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.request.UpdateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.response.LimitedUserResponse
import com.b.beep.domain.user.service.LimitedUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "제한 사용자", description = "블랙리스트 계정 관리 API")
@Validated
@RestController
@RequestMapping("/limited-users")
class LimitedUserController(
    private val limitedUserService: LimitedUserService
) {
    @Operation(summary = "제한 사용자 추가", description = "블랙리스트에 추가할 계정을 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLimitedUser(@Valid @RequestBody request: CreateLimitedUserRequest) {
        limitedUserService.createLimitedUser(request)
    }

    @Operation(summary = "제한 사용자 목록 조회", description = "블랙리스트에 추가된 계정 목록을 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getLimitedUsers(): List<LimitedUserResponse> {
        return limitedUserService.getLimitedUsers()
    }

    @Operation(summary = "제한 사용자 수정", description = "블랙리스트 계정을 수정합니다.")
    @PatchMapping("/{limitedUserId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateLimitedUser(
        @PathVariable @Positive(message = "ID는 양수여야 합니다") limitedUserId: Long,
        @Valid @RequestBody request: UpdateLimitedUserRequest
    ) {
        limitedUserService.updateLimitedUser(limitedUserId, request)
    }

    @Operation(summary = "제한 사용자 삭제", description = "블랙리스트에 있는 계정을 삭제합니다.")
    @DeleteMapping("/{limitedUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLimitedUser(@PathVariable @Positive(message = "ID는 양수여야 합니다") limitedUserId: Long) {
        limitedUserService.deleteLimitedUser(limitedUserId)
    }
}
