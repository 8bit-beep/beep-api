package com.b.beep.domain.attendance.entity

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.user.entity.UserEntity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    var room: RoomEntity? = null,

    @Column(nullable = false)
    val date: LocalDate,

    // 낙관적 락을 통한 동시성 제어
    @Version
    @Column(name = "version")
    var version: Long? = null
)
