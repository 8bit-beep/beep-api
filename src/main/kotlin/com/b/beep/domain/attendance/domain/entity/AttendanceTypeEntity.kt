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
        const val OUT_SLEEPING_TYPE_NAME = "외박"
        const val SCHOOL_EVENT_TYPE_NAME = "교내 행사"
        const val SHIFT_ATTEND_TYPE_NAME = "실이동"
        const val CLUB_TYPE_NAME = "동아리"
        const val CLASSROOM_STUDY_TYPE_NAME = "교실자습"
        const val NARSHA_TYPE_NAME = "나르샤"
        const val AFTER_SCHOOL_TYPE_NAME = "방과후"

        val ACTIVITY_ROOM_TYPE_NAMES = setOf(
            CLUB_TYPE_NAME,
            NARSHA_TYPE_NAME,
            AFTER_SCHOOL_TYPE_NAME
        )
        val COMMON_ACTIVITY_ROOM_TYPE_NAMES = setOf(CLUB_TYPE_NAME, NARSHA_TYPE_NAME)
        val SORT_MODE_TYPE_NAMES = ACTIVITY_ROOM_TYPE_NAMES + CLASSROOM_STUDY_TYPE_NAME
    }
}
