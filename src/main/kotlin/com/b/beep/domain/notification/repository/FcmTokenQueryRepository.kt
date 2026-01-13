package com.b.beep.domain.notification.repository

import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.entity.QAttendanceEntity
import com.b.beep.domain.notification.entity.FcmTokenEntity
import com.b.beep.domain.notification.entity.QFcmTokenEntity
import com.b.beep.domain.user.entity.QUserEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class FcmTokenQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val periodResolver: PeriodResolver
) {
    fun findByNotAttendStatus(): List<FcmTokenEntity> {
        val fcm = QFcmTokenEntity.fcmTokenEntity
        val user = QUserEntity.userEntity
        val attendance = QAttendanceEntity.attendanceEntity
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
            .where(
                attendance.type.eq(AttendanceType.NOT_ATTEND)
                    .or(attendance.id.isNull)
            )
            .fetch()
    }
}
