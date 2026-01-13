package com.b.beep.domain.user.service

import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.entity.StudentInfoEntity
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.domain.attendance.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.infrastructure.DAuthUserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class StudentInfoService(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
) {
    fun getOrCreateUser(dodamUser: DAuthUser): UserEntity {
        val email = dodamUser.email

        return userRepository.findByEmail(email) ?: run {
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

        return userRepository.findByEmail(email) ?: run {
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
            listOf(1, 2, 3).forEach {
                attendanceRepository.save(
                    AttendanceEntity(
                        period = it,
                        type = AttendanceType.NOT_ATTEND,
                        user = user,
                        room = null,
                        date = LocalDate.now()
                    )
                )
            }

            val newStudentInfo = StudentInfoEntity(
                user = user,
                grade = dodamUser.grade!!,
                cls = dodamUser.room!!,
                num = dodamUser.number!!
            )
            studentInfoRepository.save(newStudentInfo)
        }
    }

    fun getOrCreateStudentInfo(user: UserEntity, dodamUser: DAuthUserResponse): StudentInfoEntity {
        return studentInfoRepository.findByUser(user) ?: run {
            listOf(1, 2, 3).forEach {
                attendanceRepository.save(
                    AttendanceEntity(
                        period = it,
                        type = AttendanceType.NOT_ATTEND,
                        user = user,
                        room = null,
                        date = LocalDate.now()
                    )
                )
            }

            val newStudentInfo = StudentInfoEntity(
                user = user,
                grade = dodamUser.data.grade,
                cls = dodamUser.data.room,
                num = dodamUser.data.number
            )
            studentInfoRepository.save(newStudentInfo)
        }
    }
}
