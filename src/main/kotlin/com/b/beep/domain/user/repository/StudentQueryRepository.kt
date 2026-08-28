package com.b.beep.domain.user.repository

import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StudentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun findAllByFilters(
        grade: Int?,
        classNumber: Int?,
        keyword: String?
    ): List<StudentInfoEntity> {
        val studentInfo = QStudentInfoEntity.studentInfoEntity
        val user = QUserEntity.userEntity

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(user.isDeleted.eq(false))

        grade?.let { whereBuilder.and(studentInfo.grade.eq(it)) }
        classNumber?.let { whereBuilder.and(studentInfo.classNumber.eq(it)) }
        keyword?.trim()?.takeIf { it.isNotEmpty() }?.let { searchKeyword ->
            val keywordBuilder = BooleanBuilder(user.name.containsIgnoreCase(searchKeyword))

            if (searchKeyword.length == STUDENT_NUMBER_LENGTH && searchKeyword.all { it in '0'..'9' }) {
                val studentNumberPredicate = studentInfo.grade.eq(searchKeyword[0].digitToInt())
                    .and(studentInfo.classNumber.eq(searchKeyword[1].digitToInt()))
                    .and(studentInfo.num.eq(searchKeyword.substring(2).toInt()))
                keywordBuilder.or(studentNumberPredicate)
            }

            whereBuilder.and(keywordBuilder)
        }

        return queryFactory
            .selectFrom(studentInfo)
            .join(studentInfo.user, user).fetchJoin()
            .where(whereBuilder)
            .orderBy(
                studentInfo.grade.asc(),
                studentInfo.classNumber.asc(),
                studentInfo.num.asc()
            )
            .fetch()
    }

    companion object {
        private const val STUDENT_NUMBER_LENGTH = 4
    }
}
