package com.b.beep.domain.user.domain.entity

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import jakarta.persistence.*
import java.time.DayOfWeek

@Entity
@Table(
    name = "student_schedules",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_student_schedule",
            columnNames = ["user_id", "day_of_week", "checkpoint_id"]
        )
    ]
)
class StudentScheduleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: DayOfWeek,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    var checkpoint: AttendanceCheckpointEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    var type: AttendanceTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity
)
