package com.b.beep.domain.approval.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import com.b.beep.domain.attendance.domain.enums.Room
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "approvals")
class ApprovalEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "period", nullable = false)
    val period: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "room", nullable = false)
    val room: Room,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = true)
    var teacher: UserEntity? = null,

    val date: LocalDate
) : BaseEntity()
