package com.b.beep.domain.user.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class StudentActivityRoomError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "활동 실로 설정할 수 없는 출석 유형입니다."),
    DAY_OF_WEEK_REQUIRED(HttpStatus.BAD_REQUEST, "방과후 활동 실은 요일이 필요합니다."),
    DAY_OF_WEEK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "나르샤와 동아리 활동 실에는 요일을 지정할 수 없습니다."),
    INVALID_DAY_OF_WEEK(HttpStatus.BAD_REQUEST, "방과후 활동 실은 월~목요일만 등록할 수 있습니다."),
    DUPLICATE_ASSIGNMENT(HttpStatus.BAD_REQUEST, "동일한 활동 실 배정이 중복되었습니다.")
}
