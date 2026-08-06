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
        const val DEFAULT_ABSENCE_TYPE_NAME = "결석"
        const val NOT_ATTENDED_TYPE_NAME = "미출석"
        const val SHIFT_ATTEND_TYPE_NAME = "실이동"
        const val CLASSROOM_STUDY_TYPE_NAME = "교실자습"
    }
}
