package com.b.beep.domain.user.service

import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val contextHolder: ContextHolder,
    private val studentInfoRepository: StudentInfoRepository
) {
    @Transactional(readOnly = true)
    fun getMe(): UserEntity {
        return contextHolder.user
    }

    @Transactional(readOnly = true)
    fun getStudentInfo(user: UserEntity): StudentInfoEntity {
        return studentInfoRepository.findByUser(user)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
    }

    @Transactional(readOnly = true)
    fun getMyStudentInfo(): StudentInfoEntity {
        return getStudentInfo(contextHolder.user)
    }
}
