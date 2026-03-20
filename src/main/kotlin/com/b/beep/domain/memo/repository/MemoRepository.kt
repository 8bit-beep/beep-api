package com.b.beep.domain.memo.repository

import com.b.beep.domain.memo.domain.entity.MemoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemoRepository : JpaRepository<MemoEntity, Long> {
    fun findByGrade(grade: Int): MemoEntity?
}
