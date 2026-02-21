package com.b.beep.domain.attendance.domain.entity

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "attendances",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "checkpoint_id", "date"])
    ]
)
class AttendanceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    val checkpoint: AttendanceCheckpointEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    var type: AttendanceTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    var room: RoomEntity? = null,

    @Column(nullable = false)
    val date: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_id", nullable = true)
    val absence: AbsenceEntity? = null,

    @Version
    @Column(name = "version")
    var version: Long? = null,

    @Column(name = "is_late", nullable = false)
    var isLate: Boolean = false
) : BaseEntity()
