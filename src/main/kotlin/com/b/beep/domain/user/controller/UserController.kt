package com.b.beep.domain.user.controller

import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.user.controller.docs.UserDocs
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.service.UserService
import com.b.beep.global.common.dto.response.BaseResponse
import com.b.beep.domain.user.domain.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val attendanceQueryRepository: AttendanceQueryRepository
) : UserDocs {
    @GetMapping("/me")
    override fun getMe(): ResponseEntity<BaseResponse<UserResponse>> {
        val me = userService.getMe()
        val currentStatus = attendanceQueryRepository.findCurrentStatus(me)

        val response = UserResponse.of(me, null, currentStatus)

        if (me.role == UserRole.STUDENT) {
            val studentInfo = userService.getStudentInfo(me)
            response.studentInfo = StudentInfoResponse.of(studentInfo)
        }

        return BaseResponse.of(response)
    }
}