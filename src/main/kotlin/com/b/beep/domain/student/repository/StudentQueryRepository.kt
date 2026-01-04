package com.b.beep.domain.student.repository

import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.room.fixedroom.entity.QFixedRoomEntity
import com.b.beep.domain.user.entity.QStudentInfoEntity
import com.b.beep.domain.user.entity.QUserEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.domain.UserRole
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StudentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun findAllByStatusAndGradeAndCls(
        grade: Int,
        cls: Int,
        status: AttendanceType? = null,
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity

        return queryFactory
            .selectFrom(userEntity)
            .join(studentInfoEntity)
            .on(studentInfoEntity.user.id.eq(userEntity.id))
            .where(
                studentInfoEntity.grade.eq(grade),
                studentInfoEntity.cls.eq(cls),
                status?.let { userEntity.currentStatus.eq(it) },
                userEntity.role.eq(UserRole.STUDENT)
            )
            .fetch()
    }

    fun findAllByStatusAndRoomAndType(
        room: RoomEntity,
        type: AttendanceType,
        status: AttendanceType? = null,
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val fixedRoomEntity = QFixedRoomEntity.fixedRoomEntity

        return queryFactory
            .selectFrom(userEntity)
            .join(fixedRoomEntity)
            .on(fixedRoomEntity.user.id.eq(userEntity.id))
            .where(
                fixedRoomEntity.room.id.eq(room.id),
                fixedRoomEntity.type.eq(type),
                status?.let { userEntity.currentStatus.eq(it) },
                userEntity.role.eq(UserRole.STUDENT)
            )
            .fetch()
    }
}
