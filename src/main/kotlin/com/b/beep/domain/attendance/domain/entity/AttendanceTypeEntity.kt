package com.b.beep.domain.attendance.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "attendance_types")
class AttendanceTypeEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @Column(nullable = false)
    var isDeleted: Boolean = false
) {
    companion object {
        const val DEFAULT_ABSENCE_TYPE_NAME = "외박"
    }
}
