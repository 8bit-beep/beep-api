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
    fun findAll(date: LocalDate): List<OutSleepingQueryResult> {
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

        val managedOutSleeping = queryFactory
            .select(
                user.publicId,
                absence.reason,
                user.name,
                studentInfo.grade,
                studentInfo.classNumber,
                studentInfo.num,
                absence.startDate,
                absence.endDate,
            )
            .from(absenceUser)
            .join(absenceUser.absence, absence)
            .join(absenceUser.user, user)
            .join(studentInfo).on(studentInfo.user.id.eq(user.id))
            .where(
                user.role.eq(UserRole.STUDENT),
                user.isDeleted.isFalse,
                absence.isDeleted.isFalse,
                absence.startDate.loe(date),
                absence.endDate.goe(date),
                absence.type.name.eq(AttendanceTypeEntity.OUT_SLEEPING_TYPE_NAME),
            )
            .fetch()
            .map { row ->
                OutSleepingQueryResult(
                    publicId = row.get(user.publicId),
                    reason = requireNotNull(row.get(absence.reason)),
                    studentName = requireNotNull(row.get(user.name)),
                    grade = requireNotNull(row.get(studentInfo.grade)),
                    room = requireNotNull(row.get(studentInfo.classNumber)),
                    number = requireNotNull(row.get(studentInfo.num)),
                    startAt = requireNotNull(row.get(absence.startDate)),
                    endAt = requireNotNull(row.get(absence.endDate)),
                )
            }

        val manuallyChangedOutSleeping = queryFactory
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
                hasOutSleepingAttendance,
                hasManagedOutSleeping.not(),
            )
            .fetch()
            .map { row ->
                OutSleepingQueryResult(
                    publicId = row.get(user.publicId),
                    reason = MANUAL_OUT_SLEEPING_REASON,
                    studentName = requireNotNull(row.get(user.name)),
                    grade = requireNotNull(row.get(studentInfo.grade)),
                    room = requireNotNull(row.get(studentInfo.classNumber)),
                    number = requireNotNull(row.get(studentInfo.num)),
                    startAt = date,
                    endAt = date,
                )
            }

        return (managedOutSleeping + manuallyChangedOutSleeping).sortedWith(
            compareBy(
                { it.grade },
                { it.room },
                { it.number },
                { it.studentName },
                { it.startAt },
                { it.endAt },
            )
        )
    }

    companion object {
        const val MANUAL_OUT_SLEEPING_REASON = "일반 외박"
    }
}

data class OutSleepingQueryResult(
    val publicId: String?,
    val reason: String,
    val studentName: String,
    val grade: Int,
    val room: Int,
    val number: Int,
    val startAt: LocalDate,
    val endAt: LocalDate,
)
