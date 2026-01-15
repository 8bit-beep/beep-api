package com.b.beep.domain.notification.repository

import com.b.beep.domain.absence.domain.entity.QAbsenceEntity
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.notification.domain.entity.FcmTokenEntity
import com.b.beep.domain.notification.domain.entity.QFcmTokenEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class FcmTokenQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val periodResolver: PeriodResolver
) {
    fun findAllByNotAttendStatus(): List<FcmTokenEntity> {
        val fcm = QFcmTokenEntity.fcmTokenEntity
        val user = QUserEntity.userEntity
        val attendance = QAttendanceEntity.attendanceEntity
        val absence = QAbsenceEntity.absenceEntity
        val today = LocalDate.now()
        val period = periodResolver.getCurrentPeriod()

        if (period == 0) return emptyList()

        return queryFactory
            .selectFrom(fcm)
            .join(fcm.user, user)
            .leftJoin(attendance)
            .on(
                attendance.user.id.eq(user.id),
                attendance.date.eq(today),
                attendance.period.eq(period)
            )
            .leftJoin(absence)
            .on(
                absence.user.id.eq(user.id),
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
