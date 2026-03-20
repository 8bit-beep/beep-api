package com.b.beep.domain.memo.domain.entity

import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "memo")
class MemoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Lob @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(nullable = false)
    var isRead: Boolean = false
) : BaseEntity()
