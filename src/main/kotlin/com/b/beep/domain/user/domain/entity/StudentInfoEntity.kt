package com.b.beep.domain.user.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "student_info")
class StudentInfoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserEntity,

    var grade: Int,

    @Column(name = "class_number")
    var classNumber: Int,

    var num: Int,

    @Column(name = "card_id", unique = true)
    var cardId: String? = null,
)