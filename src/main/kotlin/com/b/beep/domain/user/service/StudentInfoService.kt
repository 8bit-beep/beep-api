package com.b.beep.domain.user.service

import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudentInfoService(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
) {
    fun getOrCreateUser(dodamUser: DAuthUser): UserEntity {
        return userRepository.findByPublicIdAndIsDeletedFalse(dodamUser.publicId)
            ?: userRepository.save(UserEntity(
                publicId = dodamUser.publicId,
                username = dodamUser.username,
                name = dodamUser.name,
                role = if (dodamUser.roles.contains("STUDENT")) UserRole.STUDENT else UserRole.TEACHER,
                profileImage = dodamUser.profileImage
            ))
    }

    fun upsertFromDodamMiniApp(dodamUser: DAuthUser): UserEntity {
        val existingUser = userRepository.findByPublicId(dodamUser.publicId)
        if (existingUser?.isDeleted == true) {
            throw CustomException(AuthError.DELETED_USER)
        }

        val user = if (existingUser == null) {
            userRepository.save(
                UserEntity(
                    publicId = dodamUser.publicId,
                    username = dodamUser.username,
                    name = dodamUser.name,
                    role = if (dodamUser.roles.contains("STUDENT")) UserRole.STUDENT else UserRole.TEACHER,
                    profileImage = dodamUser.profileImage
                )
            )
        } else {
            existingUser.username = dodamUser.username
            existingUser.name = dodamUser.name
            existingUser.profileImage = dodamUser.profileImage
            userRepository.save(existingUser)
        }

        if (user.role == UserRole.STUDENT) {
            upsertStudentInfo(user, dodamUser)
        }

        return user
    }

    fun getOrCreateStudentInfo(user: UserEntity, dodamUser: DAuthUser): StudentInfoEntity {
        return studentInfoRepository.findByUser(user) ?: run {
            studentInfoRepository.save(StudentInfoEntity(
                user = user,
                grade = dodamUser.student!!.grade,
                classNumber = dodamUser.student.room,
                num = dodamUser.student.number
            ))
        }
    }

    fun updateStudentInfo(user: UserEntity, dodamUser: DAuthUser) {
        val studentInfo = studentInfoRepository.findByUser(user) ?: return
        studentInfo.grade = dodamUser.student!!.grade
        studentInfo.classNumber = dodamUser.student.room
        studentInfo.num = dodamUser.student.number
        studentInfoRepository.save(studentInfo)
    }

    private fun upsertStudentInfo(user: UserEntity, dodamUser: DAuthUser) {
        val dodamStudent = dodamUser.student
            ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        val studentInfo = studentInfoRepository.findByUser(user)

        if (studentInfo == null) {
            studentInfoRepository.save(
                StudentInfoEntity(
                    user = user,
                    grade = dodamStudent.grade,
                    classNumber = dodamStudent.room,
                    num = dodamStudent.number
                )
            )
            return
        }

        studentInfo.grade = dodamStudent.grade
        studentInfo.classNumber = dodamStudent.room
        studentInfo.num = dodamStudent.number
        studentInfoRepository.save(studentInfo)
    }
}
