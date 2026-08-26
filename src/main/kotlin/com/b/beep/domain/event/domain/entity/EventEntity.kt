package com.b.beep.domain.event.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 교내 행사. 하루짜리 운영 데이터라 soft delete를 쓰지 않는다
 * (RoomApprovalEntity, AttendanceSortModeEntity와 같은 부류).
 */
@Entity
@Table(name = "events")
class EventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var date: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: UserEntity
) : BaseEntity()
