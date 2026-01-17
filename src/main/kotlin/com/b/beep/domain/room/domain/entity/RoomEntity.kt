package com.b.beep.domain.room.domain.entity

import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "rooms")
class RoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @Column
    var grade: Int? = null,

    @Column
    var classNumber: Int? = null,

    @Column
    var floor: Int? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false
) : BaseEntity()
