package com.b.beep.domain.user.repository

import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentInfoRepository : JpaRepository<StudentInfoEntity, Long> {
    fun findByUser(user: UserEntity): StudentInfoEntity?
    fun findByGradeAndClassNumberAndNum(grade: Int, classNumber: Int, num: Int): StudentInfoEntity?
    fun findAllByUserIsDeletedFalse(): List<StudentInfoEntity>
}
