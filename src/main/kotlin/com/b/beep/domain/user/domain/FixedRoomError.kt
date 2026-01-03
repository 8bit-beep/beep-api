package com.b.beep.domain.user.domain

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class FixedRoomError(override val status: HttpStatus, override val message: String) : CustomError {
    FIXED_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "고정실을 찾을 수 없습니다."),
    ALREADY_EXIST_ROOM(HttpStatus.BAD_REQUEST, "해당 실의 등록 내용이 이미 있습니다."),
    ALREADY_EXIST_TYPE(HttpStatus.BAD_REQUEST, "해당 출석 타입의 등록 내용이 이미 있습니다."),
    NO_PERMISSION_TO_UPDATE(HttpStatus.UNAUTHORIZED, "고정실을 변경할 권한이 없습니다."),
    ALREADY_EXIST_COMBINATION(HttpStatus.BAD_REQUEST, "이미 존재하는 조합입니다."),
}