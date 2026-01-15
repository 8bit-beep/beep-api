package com.b.beep.domain.attendance.service

import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.notification.service.NotificationService
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
// TODO 클래스 이름이랑 폴더 변경
class AttendanceScheduleService(
    private val userRepository: UserRepository,
    private val approvalRepository: ApprovalRepository,
    private val absenceRepository: AbsenceRepository,
    private val notificationService: NotificationService,
    private val attendanceRepository: AttendanceRepository,
    private val periodRepository: PeriodRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 로깅디비랑 백업디비 만들기



    /*
    * 오늘의 출석 레코드 생성
    * */
    @Transactional
    @Scheduled(cron = "0 1 0 * * MON-FRI")
    fun createAttendanceRecordToday() {
        val today = LocalDate.now()
        val periods = periodRepository.findAll().map { it.period }
        val students = userRepository.findAllByRole(UserRole.STUDENT)

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
        val periods = periodRepository.findAll().map { it.period }
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

            val attendances = attendanceRepository.findAllByUserAndDate(user, today)
            attendances.forEach { it.type = AttendanceType.OUTGOING }

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
    @Scheduled(cron = "0 35 19 * * MON-THU")  // 3교시 19:00 ~ 19:20 (금요일 제외)
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