package com.b.beep.domain.scheduling

import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.approval.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class AttendanceScheduler(
    private val userRepository: UserRepository,
    private val approvalRepository: ApprovalRepository,
    private val absenceRepository: AbsenceRepository,
    private val notificationService: NotificationService,
    private val attendanceRepository: AttendanceRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 로깅디비랑 백업디비 만들기

    /*
    * 현재 유저의 출석 상태 업데이트
    * */
    @Scheduled(cron = "1 21 13 * * MON-THU")
    @Scheduled(cron = "1 1 19 * * MON-THU")
    @Scheduled(cron = "1 1 9 * * FRI")  // 금요일 1교시
    @Transactional
    fun updateCurrentStatus() {
        val today = LocalDate.now()
        val currentPeriod = PeriodResolver.getCurrentPeriod()

        val students = userRepository.findAllByRole(UserRole.STUDENT)

        students.forEach { student ->
            val currentAttendance = attendanceRepository
                .findByPeriodAndUserAndDate(currentPeriod, student, today)

            currentAttendance?.let { attendance ->
                    // 사용자의 현재 상태 업데이트
                    student.currentStatus = attendance.type
            }
        }

        userRepository.saveAll(students)
    }

    /*
    * 실이동 수락하기
    * */
    @Scheduled(cron = "0 1 9 * * TUE-FRI")  // 1교시 (월요일 제외)
    @Scheduled(cron = "0 21 13 * * MON-THU")  // 2교시 (금요일 제외)
    fun acceptShifts() {
        val period = PeriodResolver.getCurrentPeriod()
        val attendances = attendanceRepository
            .findByPeriodAndDateAndType(period, LocalDate.now(), AttendanceType.SHIFT_ATTEND)

        attendances.forEach { attendance ->
            val user = attendance.user

            user.currentStatus = AttendanceType.SHIFT_ATTEND

            userRepository.save(user)
        }
    }

    /*
    * 오늘의 출석 레코드 생성
    * */
    @Transactional
    @Scheduled(cron = "0 1 0 * * MON-FRI")
    fun createAttendanceRecordToday() {
        val today = LocalDate.now()
        val periods = listOf(1, 2, 3)
        val students = userRepository.findAllByRole(UserRole.STUDENT)

        // 모든 학생의 currentStatus를 NOT_ATTEND로 초기화
        students.forEach { student ->
            student.currentStatus = AttendanceType.NOT_ATTEND
        }
        userRepository.saveAll(students)

        val records = students.flatMap { student ->
            periods.map { period ->
                AttendanceEntity(
                    period = period,
                    type = AttendanceType.NOT_ATTEND,
                    user = student,
                    date = today
                )
            }
        }
        attendanceRepository.saveAll(records)
    }

    /*
    * 실 승인 데이터 생성
    * */
    @Scheduled(cron = "0 10 0 * * MON-FRI")
    @Transactional
    fun createApprovalsToday() {
        val today = LocalDate.now()
        val periods = listOf(1, 2, 3)
        val rooms = Room.entries.toTypedArray()

        val approvals = rooms.flatMap { room ->
            periods.map { period ->
                ApprovalEntity(
                    room = room,
                    period = period,
                    date = today
                )
            }
        }
        approvalRepository.saveAll(approvals)
    }

    @Scheduled(cron = "0 20 0 * * MON-FRI")
    @Transactional
    fun changeStatusAbsenceStudents() {
        val today = LocalDate.now()
        val absences = absenceRepository.findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)

        absences.forEach { absence ->
            val user = absence.user

            user.currentStatus = AttendanceType.OUTGOING
            userRepository.save(user)

            // 출석 기록(1~3교시)도 외박으로 변경
            val attendances = attendanceRepository.findByUserAndDate(user, today)
            attendances
                .forEach { it.type = AttendanceType.OUTGOING }

            attendanceRepository.saveAll(attendances)
        }
    }

    // 출석 시간 알림 전송
    @Scheduled(cron = "0 0 9 * * TUE-FRI")  // 1교시 9:00 ~ 9:20 (월요일 제외)
    @Scheduled(cron = "0 20 13 * * MON-THU")  // 2교시 13:20 ~ 13:40 (금요일 제외)
    @Scheduled(cron = "0 0 19 * * MON-THU")  // 3교시 19:00 ~ 19:20 (금요일 제외)
    fun sendAttendanceNotificationToAll() {
        logger.info("출석 알림 전송 시작 - 전체 학생")
        notificationService.sendToAll(
            title = "출석 시간입니다! 출석하세요",
            body = "지금부터 20분 간 출석이 가능합니다.",
            imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
        )
        logger.info("출석 알림 전송 완료 - 전체 학생")
    }

    // 미출석자 알림 전송
    @Scheduled(cron = "0 15 9 * * TUE-FRI")  // 1교시 9:00 ~ 9:20 (월요일 제외)
    @Scheduled(cron = "0 35 13 * * MON-THU")  // 2교시 13:20 ~ 13:40 (금요일 제외)
    @Scheduled(cron = "0 15 19 * * MON-THU")  // 3교시 19:00 ~ 19:20 (금요일 제외)
    fun sendAttendanceReminderToNotAttended() {
        logger.info("미출석자 알림 전송 시작")
        notificationService.sendToNotAttended(
            title = "아직 출석하지 않았습니다!",
            body = "출석 시간이 5분 후 종료됩니다. 지금 출석해주세요.",
            imageUrl = "https://www.gstatic.com/mobilesdk/240501_mobilesdk/firebase_28dp.png"
        )
        logger.info("미출석자 알림 전송 완료")
    }
}
