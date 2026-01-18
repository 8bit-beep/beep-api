package com.b.beep.domain.room.domain.entity

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "room_approvals",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_room_approval_checkpoint_room_date",
            columnNames = ["checkpoint_id", "room_id", "date"]
        )
    ]
)
class RoomApprovalEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    val checkpoint: AttendanceCheckpointEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: RoomEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = true)
    var teacher: UserEntity? = null,

    val date: LocalDate,
) : BaseEntity()
