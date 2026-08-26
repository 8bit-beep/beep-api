package com.b.beep.domain.memo.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "memo")
class MemoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val grade: Int,

    // eventBlock + manualContent를 합친 최종 표시 텍스트
    @Lob @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    var content: String = "",

    // 행사 등록/수정/삭제 때마다 통째로 다시 채워지는 자동 영역
    @Lob @Column(name = "event_block", columnDefinition = "MEDIUMTEXT", nullable = false)
    var eventBlock: String = "",

    // 교사가 직접 쓴 영역. 행사 변경에 영향받지 않는다
    @Lob @Column(name = "manual_content", columnDefinition = "MEDIUMTEXT", nullable = false)
    var manualContent: String = "",

    @Column(nullable = false)
    var isRead: Boolean = false
)
