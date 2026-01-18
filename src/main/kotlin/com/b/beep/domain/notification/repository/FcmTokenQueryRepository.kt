package com.b.beep.domain.notification.repository

import com.b.beep.domain.absence.domain.entity.QAbsenceEntity
import com.b.beep.domain.absence.domain.entity.QAbsenceUserEntity
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.notification.domain.entity.FcmTokenEntity
import com.b.beep.domain.notification.domain.entity.QFcmTokenEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneId

@Repository
class FcmTokenQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val checkpointResolver: CheckpointResolver
) {
    fun findAllByNotAttendStatus(): List<FcmTokenEntity> {
        val fcm = QFcmTokenEntity.fcmTokenEntity
        val user = QUserEntity.userEntity
        val attendance = QAttendanceEntity.attendanceEntity
        val absenceUser = QAbsenceUserEntity.absenceUserEntity
        val absence = QAbsenceEntity.absenceEntity
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNull() ?: return emptyList()

        return queryFactory
            .selectFrom(fcm)
            .join(fcm.user, user)
            .leftJoin(attendance)
            .on(
                attendance.user.id.eq(user.id),
                attendance.date.eq(today),
                attendance.checkpoint.id.eq(checkpoint.id)
            )
            .leftJoin(absenceUser)
            .on(absenceUser.user.id.eq(user.id))
            .leftJoin(absence)
            .on(
                absence.id.eq(absenceUser.absence.id),
                absence.startDate.loe(today),
                absence.endDate.goe(today)
            )
            .where(
                attendance.id.isNull
                    .and(absence.id.isNull)
            )
            .fetch()
    }
}
