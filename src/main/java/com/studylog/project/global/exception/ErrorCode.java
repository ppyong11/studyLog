package com.studylog.project.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("USER_404-001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    ID_DUPLICATE("USER-409-001", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    NICKNAME_DUPLICATE("USER-409-002", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    MAIL_DUPLICATE("USER-409-003", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH("USER-401-001", "현재 비밀번호와 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    PASSWORD_SAME_AS_OLD("USER-400-001", "새 비밀번호가 현재 비밀번호와 일치합니다.", HttpStatus.BAD_REQUEST),

    MAIL_SEND_FAILED("MAIL-500-001", "메일 전송에 실패했습니다.",  HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAIL("LOGIN_401-001", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_DELETED("USER-410-001", "회원 탈퇴 철회 가능 기간이 만료되었습니다.", HttpStatus.GONE),
    JWT_EXPIRED("JWT-401-001", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_SIGNATURE("JWT-401-002", "잘못된 JWT 서명입니다.", HttpStatus.UNAUTHORIZED),
    MALFORMED_JWT("JWT-401-003", "잘못된 형식의 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT("JWT-401-004", "지원하지 않는 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    LOGGED_OUT_TOKEN("JWT-401-005", "로그아웃된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH-403-001", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    AUTH_REQUIRED("AUTH-401-001", "로그인이 필요한 요청입니다.", HttpStatus.UNAUTHORIZED),
    MAIL_EXPIRED("MAIL_EXPIRED", "인증 코드가 만료되었습니다.", HttpStatus.BAD_REQUEST),
    BOARD_NOT_FOUND("BOARD_404-001", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND("FILE_404-001", "존재하지 않는 파일입니다.", HttpStatus.NOT_FOUND),
    PLAN_NOT_FOUND("PLAN_404-001", "존재하지 않는 계획입니다.", HttpStatus.NOT_FOUND),
    TIMER_NOT_FOUND("TIMER_404-001", "존재하지 않는 타이머입니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("CATEGORY-404-001", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_MODIFIABLE("CATEGORY-400-001", "해당 카테고리는 변경 및 삭제가 불가합니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_DUPLICATE("CATEGORY-409-001", "동일한 카테고리명이 존재합니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("REQ-400-001", "잘못된 요청입니다. 요청 정보를 다시 한 번 확인해 주세요.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
