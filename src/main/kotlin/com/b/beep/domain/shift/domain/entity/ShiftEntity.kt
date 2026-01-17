package com.b.beep.domain.shift.domain.entity

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "shifts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_shift_user_date_checkpoint",
            columnNames = ["user_id", "date", "checkpoint_id"]
        )
    ]
)
class ShiftEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    var checkpoint: AttendanceCheckpointEntity,

    var reason: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ShiftStatus,

    var date: LocalDate,
) : BaseEntity()
