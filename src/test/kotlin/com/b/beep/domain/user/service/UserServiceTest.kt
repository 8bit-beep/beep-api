package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.notification.repository.FcmTokenRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var contextHolder: ContextHolder

    @Mock
    private lateinit var studentInfoRepository: StudentInfoRepository

    @Mock
    private lateinit var attendanceQueryRepository: AttendanceQueryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var studentScheduleRepository: StudentScheduleRepository

    @Mock
    private lateinit var studentActivityRoomRepository: StudentActivityRoomRepository

    @Mock
    private lateinit var fcmTokenRepository: FcmTokenRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `내 정보 조회는 학생 학년의 현재 출석 상태를 반환한다`() {
        val user = UserEntity(
            id = 1L,
            username = "student",
            name = "학생",
            role = UserRole.STUDENT
        )
        val studentInfo = StudentInfoEntity(
            id = 1L,
            user = user,
            grade = 1,
            classNumber = 2,
            num = 3
        )
        val club = AttendanceTypeEntity(id = 1L, name = AttendanceTypeEntity.CLUB_TYPE_NAME)
        whenever(contextHolder.user).thenReturn(user)
        whenever(studentInfoRepository.findByUser(user)).thenReturn(studentInfo)
        whenever(attendanceQueryRepository.findCurrentStatus(user, studentInfo.grade)).thenReturn(club)

        val result = userService.getMe()

        assertEquals(studentInfo.grade, result.studentInfo?.grade)
        assertEquals(AttendanceTypeEntity.CLUB_TYPE_NAME, result.currentStatus?.name)
        verify(attendanceQueryRepository).findCurrentStatus(user, studentInfo.grade)
    }

    @Test
    fun `사용자 삭제는 운영 데이터만 정리하고 사용자와 학적 정보는 보존한다`() {
        val user = UserEntity(id = 1L, username = "student", name = "학생", role = UserRole.STUDENT)
        whenever(userRepository.findByIdAndIsDeletedFalse(user.id!!)).thenReturn(user)

        userService.deleteUser(user.id!!)

        assertTrue(user.isDeleted)
        verify(studentScheduleRepository).deleteAllByUser(user)
        verify(studentActivityRoomRepository).deleteAllByUser(user)
        verify(fcmTokenRepository).deleteAllByUser(user)
        verify(userRepository).findByIdAndIsDeletedFalse(user.id!!)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(studentInfoRepository, attendanceQueryRepository)
    }

    @Test
    fun `사용자가 없거나 이미 삭제되었으면 운영 데이터를 정리하지 않는다`() {
        whenever(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

        val exception = assertThrows(CustomException::class.java) {
            userService.deleteUser(1L)
        }

        assertEquals(UserError.USER_NOT_FOUND, exception.error)
        verifyNoInteractions(studentScheduleRepository, studentActivityRoomRepository, fcmTokenRepository)
    }

    @Test
    fun `운영 데이터 정리에 실패하면 삭제 완료 상태로 바꾸지 않는다`() {
        val user = UserEntity(id = 1L, username = "student", name = "학생", role = UserRole.STUDENT)
        whenever(userRepository.findByIdAndIsDeletedFalse(user.id!!)).thenReturn(user)
        doThrow(IllegalStateException("알림 토큰 정리 실패"))
            .whenever(fcmTokenRepository).deleteAllByUser(user)

        assertThrows(IllegalStateException::class.java) {
            userService.deleteUser(user.id!!)
        }

        assertFalse(user.isDeleted)
    }
}
