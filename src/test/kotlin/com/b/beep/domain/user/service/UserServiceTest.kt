package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.security.ContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
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
}
