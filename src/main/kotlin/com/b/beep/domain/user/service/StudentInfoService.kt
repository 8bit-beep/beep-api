package com.b.beep.domain.user.service

import com.b.beep.domain.auth.infrastructure.DAuthUser
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
        return userRepository.findByPublicIdAndIsDeletedFalse(dodamUser.publicId)
            ?: userRepository.save(UserEntity(
                publicId = dodamUser.publicId,
                username = dodamUser.username,
                name = dodamUser.name,
                role = if (dodamUser.roles.contains("STUDENT")) UserRole.STUDENT else UserRole.TEACHER,
                profileImage = dodamUser.profileImage
            ))
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
}
