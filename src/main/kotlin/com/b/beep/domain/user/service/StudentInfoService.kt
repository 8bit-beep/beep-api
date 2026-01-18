package com.b.beep.domain.user.service

import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.infrastructure.DAuthUserResponse
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudentInfoService(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
) {
    fun getOrCreateUser(dodamUser: DAuthUser): UserEntity {
        val email = dodamUser.email

        return userRepository.findByEmailAndIsDeletedFalse(email) ?: run {
            val newUser = UserEntity(
                email = email,
                username = dodamUser.name,
                role = if (dodamUser.role == "STUDENT") UserRole.STUDENT else UserRole.TEACHER,
                profileImage = dodamUser.profileImage
            )
            userRepository.save(newUser)
        }
    }

    fun getOrCreateUser(dodamUser: DAuthUserResponse): UserEntity {
        val email = dodamUser.data.email

        return userRepository.findByEmailAndIsDeletedFalse(email) ?: run {
            val newUser = UserEntity(
                email = email,
                username = dodamUser.data.name,
                role = if (dodamUser.data.role == "STUDENT") UserRole.STUDENT else UserRole.TEACHER,
                profileImage = dodamUser.data.profileImage
            )
            userRepository.save(newUser)
        }
    }

    fun getOrCreateStudentInfo(user: UserEntity, dodamUser: DAuthUser): StudentInfoEntity {
        return studentInfoRepository.findByUser(user) ?: run {
            val newStudentInfo = StudentInfoEntity(
                user = user,
                grade = dodamUser.grade!!,
                classNumber = dodamUser.room!!,
                num = dodamUser.number!!
            )
            studentInfoRepository.save(newStudentInfo)
        }
    }

    fun getOrCreateStudentInfo(user: UserEntity, dodamUser: DAuthUserResponse): StudentInfoEntity {
        return studentInfoRepository.findByUser(user) ?: run {
            val newStudentInfo = StudentInfoEntity(
                user = user,
                grade = dodamUser.data.grade,
                classNumber = dodamUser.data.room,
                num = dodamUser.data.number
            )
            studentInfoRepository.save(newStudentInfo)
        }
    }
}
