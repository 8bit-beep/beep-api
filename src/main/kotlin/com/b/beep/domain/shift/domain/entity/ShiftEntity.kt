package com.b.beep.domain.shift.domain.entity

import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "shifts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "date", "period"])
    ]
)
class ShiftEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "room", nullable = false)
    var room: Room,

    @Column(name = "period", nullable = false)
    var period: Int,

    var reason: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ShiftStatus,

    var date: LocalDate,
) : BaseEntity()