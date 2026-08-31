package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.QAbsenceEntity
import com.b.beep.domain.absence.domain.entity.QAbsenceUserEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class OutSleepingQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    fun findAllStudents(date: LocalDate): List<OutSleepingStudentQueryResult> {
        val user = QUserEntity.userEntity
        val studentInfo = QStudentInfoEntity.studentInfoEntity
        val absenceUser = QAbsenceUserEntity.absenceUserEntity
        val absence = QAbsenceEntity.absenceEntity
        val attendance = QAttendanceEntity.attendanceEntity

        val hasManagedOutSleeping = JPAExpressions
            .selectOne()
            .from(absenceUser)
            .join(absenceUser.absence, absence)
            .where(
                absenceUser.user.id.eq(user.id),
                absence.isDeleted.isFalse,
                absence.startDate.loe(date),
                absence.endDate.goe(date),
                absence.type.name.eq(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME),
            )
            .exists()

        val hasOutSleepingAttendance = JPAExpressions
            .selectOne()
            .from(attendance)
            .where(
                attendance.user.id.eq(user.id),
                attendance.date.eq(date),
                attendance.type.name.eq(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME),
            )
            .exists()

        return queryFactory
            .select(
                user.publicId,
                user.name,
                studentInfo.grade,
                studentInfo.classNumber,
                studentInfo.num,
            )
            .from(user)
            .join(studentInfo).on(studentInfo.user.id.eq(user.id))
            .where(
                user.role.eq(UserRole.STUDENT),
                user.isDeleted.isFalse,
                hasManagedOutSleeping.or(hasOutSleepingAttendance),
            )
            .distinct()
            .orderBy(
                studentInfo.grade.asc(),
                studentInfo.classNumber.asc(),
                studentInfo.num.asc(),
                user.name.asc(),
                user.id.asc(),
            )
            .fetch()
            .map { row ->
                OutSleepingStudentQueryResult(
                    publicId = row.get(user.publicId),
                    name = requireNotNull(row.get(user.name)),
                    grade = requireNotNull(row.get(studentInfo.grade)),
                    room = requireNotNull(row.get(studentInfo.classNumber)),
                    number = requireNotNull(row.get(studentInfo.num)),
                )
            }
    }
}

data class OutSleepingStudentQueryResult(
    val publicId: String?,
    val name: String,
    val grade: Int,
    val room: Int,
    val number: Int,
)
