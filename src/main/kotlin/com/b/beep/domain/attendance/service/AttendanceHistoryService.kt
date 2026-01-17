package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.attendance.controller.dto.response.history.ClassAttendanceHistoryResponse
import com.b.beep.domain.attendance.controller.dto.response.history.PeriodStatus
import com.b.beep.domain.attendance.controller.dto.response.history.RoomAttendanceHistoryResponse
import com.b.beep.domain.attendance.controller.dto.response.history.StudentAttendanceRecord
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
    private val checkpointRepository: AttendanceCheckpointRepository
) {
    fun getByClass(date: LocalDate): List<ClassAttendanceHistoryResponse> {
        val students = userRepository.findAllByRole(UserRole.STUDENT)
        val checkpoints = checkpointRepository.findAll().sortedBy { it.id }

        val records = students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val statuses = checkpoints.map { checkpoint ->
                val attendance = attendances.find { it.checkpoint.id == checkpoint.id }
                PeriodStatus(
                    checkpointId = checkpoint.id!!,
                    checkpointName = checkpoint.name,
                    status = attendance?.type?.name ?: "NOT_RECORDED"
                )
            }

            Triple(
                "${studentInfo.grade}-${studentInfo.classNumber}",
                studentInfo.num,
                StudentAttendanceRecord(
                    username = student.username,
                    studentId = "${studentInfo.grade}${studentInfo.classNumber}${
                        String.format(
                            "%02d",
                            studentInfo.num
                        )
                    }",
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
        val checkpoints = checkpointRepository.findAll().sortedBy { it.id }
        val dayOfWeek = date.dayOfWeek

        val records = students.flatMap { student ->
            val schedules = studentScheduleRepository.findAllByUser(student)
            val todaySchedules = schedules.filter { it.dayOfWeek == dayOfWeek }

            todaySchedules.map { schedule ->
                val attendances = attendanceRepository.findAllByUserAndDate(student, date)
                val studentInfo = studentInfoRepository.findByUser(student)

                val statuses = checkpoints.map { checkpoint ->
                    val attendance = attendances.find { it.checkpoint.id == checkpoint.id }
                    PeriodStatus(
                        checkpointId = checkpoint.id!!,
                        checkpointName = checkpoint.name,
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
        val checkpoints = checkpointRepository.findAll().sortedBy { it.id }

        return students.mapNotNull { student ->
            val studentInfo = studentInfoRepository.findByUser(student) ?: return@mapNotNull null
            val attendances = attendanceRepository.findAllByUserAndDate(student, date)

            val statuses = checkpoints.map { checkpoint ->
                val attendance = attendances.find { it.checkpoint.id == checkpoint.id }
                PeriodStatus(
                    checkpointId = checkpoint.id!!,
                    checkpointName = checkpoint.name,
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