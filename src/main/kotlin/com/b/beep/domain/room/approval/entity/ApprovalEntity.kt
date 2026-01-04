package com.b.beep.domain.room.approval.entity

import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "approvals")
class ApprovalEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "period", nullable = false)
    val period: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: RoomEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = true)
    var teacher: UserEntity? = null,

    @Column(name =  "date", nullable = false)
    val date: LocalDate
) : BaseEntity()
