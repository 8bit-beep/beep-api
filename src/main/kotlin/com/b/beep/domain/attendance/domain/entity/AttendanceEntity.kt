package com.b.beep.domain.attendance.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "attendances",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "period", "date"])
    ]
)
class AttendanceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "period", nullable = false)
    val period: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: AttendanceType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "room", nullable = true)
    var room: Room? = null,

    @Column(nullable = false)
    val date: LocalDate,

    // 낙관적 락을 통한 동시성 제어
    @Version
    @Column(name = "version")
    var version: Long? = null
)
