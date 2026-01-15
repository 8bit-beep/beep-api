package com.b.beep.domain.history.service

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.history.controller.dto.response.ClassAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.RoomAttendanceHistoryResponse
import com.b.beep.domain.history.controller.dto.response.StudentAttendanceRecord
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
    private val studentScheduleRepository: StudentScheduleRepository
) {
    fun getByClass(date: LocalDate): List<ClassAttendanceHistoryResponse> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)

        val records = students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val period1 = attendances.find { it.period == 1 }?.type?.toDisplayName() ?: "미기록"
            val period2 = attendances.find { it.period == 2 }?.type?.toDisplayName() ?: "미기록"
            val period3 = attendances.find { it.period == 3 }?.type?.toDisplayName() ?: "미기록"

            val finalStatus = if (period1 != "미출석" && period2 != "미출석" && period3 != "미출석") "출석" else "미출석"

            Triple(
                "${studentInfo.grade}-${studentInfo.classNumber}",
                studentInfo.num.toString(),
                StudentAttendanceRecord(
                    studentNumber = studentInfo.num.toString(),
                    studentName = student.username,
                    period1 = period1,
                    period2 = period2,
                    period3 = period3,
                    finalStatus = finalStatus
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
        val dayOfWeek = date.dayOfWeek

        val records = students.flatMap { student ->
            val schedules = studentScheduleRepository.findAllByUser(student)
            val todaySchedules = schedules.filter { it.dayOfWeek == dayOfWeek }

            todaySchedules.map { schedule ->
                val attendances = attendanceRepository.findAllByUserAndDate(student, date)

                val period1 = attendances.find { it.period == 1 }?.type?.toDisplayName() ?: "미기록"
                val period2 = attendances.find { it.period == 2 }?.type?.toDisplayName() ?: "미기록"
                val period3 = attendances.find { it.period == 3 }?.type?.toDisplayName() ?: "미기록"

                val finalStatus = if (period1 != "미출석" && period2 != "미출석" && period3 != "미출석") "출석" else "미출석"

                val studentInfo = studentInfoRepository.findByUser(student)

                Pair(
                    schedule.room.name,
                    StudentAttendanceRecord(
                        studentNumber = studentInfo?.num?.toString() ?: "",
                        studentName = student.username,
                        period1 = period1,
                        period2 = period2,
                        period3 = period3,
                        finalStatus = finalStatus
                    )
                )
            }
        }

        return records
            .groupBy { it.first }
            .map { (room, list) ->
                RoomAttendanceHistoryResponse(
                    room = room,
                    students = list.sortedBy { it.second.studentNumber }.map { it.second }
                )
            }
            .sortedBy { it.room }
    }

    fun getAll(date: LocalDate): List<StudentAttendanceRecord> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)

        return students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val period1 = attendances.find { it.period == 1 }?.type?.toDisplayName() ?: "미기록"
            val period2 = attendances.find { it.period == 2 }?.type?.toDisplayName() ?: "미기록"
            val period3 = attendances.find { it.period == 3 }?.type?.toDisplayName() ?: "미기록"

            val finalStatus = if (period1 != "미출석" && period2 != "미출석" && period3 != "미출석") "출석" else "미출석"

            StudentAttendanceRecord(
                studentNumber = "${studentInfo.grade}${studentInfo.classNumber}${
                    String.format(
                        "%02d",
                        studentInfo.num
                    )
                }",
                studentName = student.username,
                period1 = period1,
                period2 = period2,
                period3 = period3,
                finalStatus = finalStatus
            )
        }.sortedBy { it.studentNumber }
    }

    private fun AttendanceType.toDisplayName(): String {
        return when (this) {
            AttendanceType.CLUB -> "동아리"
            AttendanceType.CLASS -> "출석"
            AttendanceType.NOT_ATTEND -> "미출석"
            AttendanceType.SLEEPOVER -> "외박"
            AttendanceType.OUTGOING -> "외출"
            AttendanceType.AFTER_SCHOOL -> "방과후"
            AttendanceType.NARSHA -> "나르샤"
            AttendanceType.FIELD_PRACTICE -> "현장실습"
            AttendanceType.SHIFT_ATTEND -> "실이동출석"
            AttendanceType.OTHER -> "기타"
            AttendanceType.INDUSTRY -> "산학"
            AttendanceType.WINTER_CAMP_SELF_STUDY -> "겨울캠프자습"
            AttendanceType.WINTER_CAMP_LECTURE -> "겨울캠프강의"
        }
    }
}
