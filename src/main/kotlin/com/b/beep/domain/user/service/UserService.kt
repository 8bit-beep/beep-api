package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val contextHolder: ContextHolder,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceQueryRepository: AttendanceQueryRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getMe(): UserResponse {
        return contextHolder.user.toResponse()
    }

    fun deleteUser(userId: Long) {
        val user = userRepository.findByIdAndIsDeletedFalse(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
        user.isDeleted = true
    }

    private fun UserEntity.toResponse(): UserResponse {
        val studentInfo = if (role == UserRole.STUDENT) {
            StudentInfoResponse.of(getStudentInfo(this))
        } else null
        val currentStatus = attendanceQueryRepository.findCurrentStatus(this)

        return UserResponse(
            id = id,
            username = username,
            name = name,
            role = role,
            profileImage = profileImage,
            studentInfo = studentInfo,
            currentStatus = currentStatus?.let { AttendanceTypeResponse.of(it) }
        )
    }

    private fun getStudentInfo(user: UserEntity): StudentInfoEntity {
        return studentInfoRepository.findByUser(user)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
    }
}
