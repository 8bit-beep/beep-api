package com.b.beep.domain.room.fixedroom.entity

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.user.entity.UserEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "fixed_rooms",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "room_id"])
    ]
)
class FixedRoomEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: AttendanceType,
)
