package com.b.beep.domain.history.service

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.history.controller.dto.response.ClassAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.PeriodStatus
import com.b.beep.domain.history.controller.dto.response.RoomAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.StudentAttendanceRecord
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class AttendanceHistoryService(
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val periodRepository: PeriodRepository
) {
    fun getByClass(date: LocalDate): List<ClassAttendanceHistoryResponse> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)
        val periods = periodRepository.findAll().sortedBy { it.period }

        val records = students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val statuses = periods.map { period ->
                val attendance = attendances.find { it.period == period.period }
                PeriodStatus(
                    period = period.period,
                    status = attendance?.type?.name ?: "NOT_RECORDED"
                )
            }

            Triple(
                "${studentInfo.grade}-${studentInfo.classNumber}",
                studentInfo.num,
                StudentAttendanceRecord(
                    username = student.username,
                    studentId = "${studentInfo.grade}${studentInfo.classNumber}${String.format("%02d", studentInfo.num)}",
                    statuses = statuses
                )
            )
        }

        return records
            .groupBy { it.first }
            .map { (classification, list) ->
                ClassAttendanceHistoryResponse(
                    classification = classification,
                    students = list.sortedBy { it.second }.map { it.third }
                )
            }
            .sortedBy { it.classification }
    }

    fun getByRoom(date: LocalDate): List<RoomAttendanceHistoryResponse> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)
        val periods = periodRepository.findAll().sortedBy { it.period }
        val dayOfWeek = date.dayOfWeek

        val records = students.flatMap { student ->
            val schedules = studentScheduleRepository.findAllByUser(student)
            val todaySchedules = schedules.filter { it.dayOfWeek == dayOfWeek }

            todaySchedules.map { schedule ->
                val attendances = attendanceRepository.findAllByUserAndDate(student, date)
                val studentInfo = studentInfoRepository.findByUser(student)

                val statuses = periods.map { period ->
                    val attendance = attendances.find { it.period == period.period }
                    PeriodStatus(
                        period = period.period,
                        status = attendance?.type?.name ?: "NOT_RECORDED"
                    )
                }

                Pair(
                    schedule.room.name,
                    StudentAttendanceRecord(
                        username = student.username,
                        studentId = studentInfo?.let {
                            "${it.grade}${it.classNumber}${String.format("%02d", it.num)}"
                        } ?: "",
                        statuses = statuses
                    )
                )
            }
        }

        return records
            .groupBy { it.first }
            .map { (room, list) ->
                RoomAttendanceHistoryResponse(
                    room = room,
                    students = list.sortedBy { it.second.studentId }.map { it.second }
                )
            }
            .sortedBy { it.room }
    }

    fun getAll(date: LocalDate): List<StudentAttendanceRecord> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)
        val periods = periodRepository.findAll().sortedBy { it.period }

        return students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val statuses = periods.map { period ->
                val attendance = attendances.find { it.period == period.period }
                PeriodStatus(
                    period = period.period,
                    status = attendance?.type?.name ?: "NOT_RECORDED"
                )
            }

            StudentAttendanceRecord(
                username = student.username,
                studentId = "${studentInfo.grade}${studentInfo.classNumber}${String.format("%02d", studentInfo.num)}",
                statuses = statuses
            )
        }.sortedBy { it.studentId }
    }
}
