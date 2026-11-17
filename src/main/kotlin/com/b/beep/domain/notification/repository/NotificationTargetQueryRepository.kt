package com.b.beep.domain.notification.repository

import com.b.beep.domain.absence.domain.entity.QAbsenceEntity
import com.b.beep.domain.absence.domain.entity.QAbsenceUserEntity
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneId

@Repository
class NotificationTargetQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val checkpointResolver: CheckpointResolver,
) {
    fun findAllActiveStudentPublicIds(grades: List<Int> = TARGET_GRADES): List<String> {
        if (grades.isEmpty()) return emptyList()

        val studentInfo = QStudentInfoEntity.studentInfoEntity
        val user = QUserEntity.userEntity

        return queryFactory
            .select(user.publicId)
            .from(studentInfo)
            .join(studentInfo.user, user)
            .where(
                studentInfo.grade.`in`(grades),
                user.isDeleted.isFalse,
                user.publicId.isNotNull,
            )
            .distinct()
            .fetch()
            .filterNotNull()
    }

    fun findAllNotAttendedPublicIds(): List<String> {
        val checkpointId = checkpointResolver.getCurrentCheckpointOrNull()?.id ?: return emptyList()
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        return findAllNotAttendedPublicIds(checkpointId, today, TARGET_GRADES)
    }

    fun findAllNotAttendedPublicIds(checkpointId: Long, date: LocalDate, grades: List<Int>): List<String> {
        if (grades.isEmpty()) return emptyList()

        val studentInfo = QStudentInfoEntity.studentInfoEntity
        val user = QUserEntity.userEntity
        val attendance = QAttendanceEntity.attendanceEntity
        val absenceUser = QAbsenceUserEntity.absenceUserEntity
        val absence = QAbsenceEntity.absenceEntity

        val hasAttendance = JPAExpressions
            .selectOne()
            .from(attendance)
            .where(
                attendance.user.id.eq(user.id),
                attendance.checkpoint.id.eq(checkpointId),
                attendance.date.eq(date),
            )
            .exists()

        val hasActiveAbsence = JPAExpressions
            .selectOne()
            .from(absenceUser)
            .join(absenceUser.absence, absence)
            .where(
                absenceUser.user.id.eq(user.id),
                absence.startDate.loe(date),
                absence.endDate.goe(date),
                absence.isDeleted.isFalse,
            )
            .exists()

        return queryFactory
            .select(user.publicId)
            .from(studentInfo)
            .join(studentInfo.user, user)
            .where(
                studentInfo.grade.`in`(grades),
                user.isDeleted.isFalse,
                user.publicId.isNotNull,
                hasAttendance.not(),
                hasActiveAbsence.not(),
            )
            .distinct()
            .fetch()
            .filterNotNull()
    }

    companion object {
        private val TARGET_GRADES = listOf(1, 2)
    }
}
