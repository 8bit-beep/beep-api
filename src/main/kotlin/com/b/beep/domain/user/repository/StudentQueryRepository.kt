package com.b.beep.domain.user.repository

import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class StudentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun findAllByFilters(
        grade: Int?,
        classNumber: Int?,
        keyword: String?,
        pageable: Pageable
    ): Page<StudentInfoEntity> {
        val studentInfo = QStudentInfoEntity.studentInfoEntity
        val user = QUserEntity.userEntity

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(user.isDeleted.eq(false))

        grade?.let { whereBuilder.and(studentInfo.grade.eq(it)) }
        classNumber?.let { whereBuilder.and(studentInfo.classNumber.eq(it)) }
        keyword?.let { whereBuilder.and(user.username.containsIgnoreCase(it)) }

        val content = queryFactory
            .selectFrom(studentInfo)
            .join(studentInfo.user, user).fetchJoin()
            .where(whereBuilder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(studentInfo.grade.asc(), studentInfo.classNumber.asc(), studentInfo.num.asc())
            .fetch()

        val countQuery = queryFactory
            .select(studentInfo.count())
            .from(studentInfo)
            .join(studentInfo.user, user)
            .where(whereBuilder)

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
