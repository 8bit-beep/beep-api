package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.LimitedUserDocs
import com.b.beep.domain.user.controller.dto.request.CreateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.request.UpdateLimitedUserRequest
import com.b.beep.domain.user.controller.dto.response.LimitedUserResponse
import com.b.beep.domain.user.service.LimitedUserService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/limited-users")
class LimitedUserController(
    private val limitedUserService: LimitedUserService
) : LimitedUserDocs {
    @PostMapping
    override fun createLimitedUser(@Valid @RequestBody request: CreateLimitedUserRequest) {
        limitedUserService.createLimitedUser(request)
    }

    @GetMapping
    override fun getLimitedUsers(): List<LimitedUserResponse> {
        return limitedUserService.getLimitedUsers()
    }

    @PatchMapping("/{limitedUserId}")
    override fun updateLimitedUser(@PathVariable @Positive(message = "ID는 양수여야 합니다") limitedUserId: Long,
                                   @Valid @RequestBody request: UpdateLimitedUserRequest) {
        limitedUserService.updateLimitedUser(limitedUserId, request)
    }

    @DeleteMapping("/{limitedUserId}")
    override fun deleteLimitedUser(@PathVariable @Positive(message = "ID는 양수여야 합니다") limitedUserId: Long) {
        limitedUserService.deleteLimitedUser(limitedUserId)
    }
}