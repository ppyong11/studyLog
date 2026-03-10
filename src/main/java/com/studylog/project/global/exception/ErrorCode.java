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
    DATE_RANGE_REQUIRED("DATE-400-001", "조회 범위를 입력해 주세요.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("DATE-400-003", "시작 날짜가 종료 날짜보다 뒤일 수 없습니다.", HttpStatus.BAD_REQUEST),

    MAIL_SEND_FAILED("MAIL-500-001", "메일 전송에 실패했습니다.",  HttpStatus.INTERNAL_SERVER_ERROR),
    MAIL_ALREADY_VERIFIED("MAIL-400-001", "이미 인증이 완료된 메일입니다. 나중에 다시 시도해 주세요.", HttpStatus.BAD_REQUEST),
    MAIL_CODE_MISMATCH("MAIL-400-003", "인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MAIL_CODE_EXPIRED("MAIL-400-004", "인증 코드가 없거나 만료되었습니다.", HttpStatus.BAD_REQUEST),
    MAIL_CODE_REQUIRED("MAIL-400-002", "인증 코드를 입력하세요.", HttpStatus.BAD_REQUEST),

    AUTH_SESSION_EXPIRED("AUTH-401-002", "인증이 만료됐거나 인증된 메일이 아닙니다.₩n회원가입을 다시 진행해 주세요.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAIL("LOGIN_401-001", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_DELETED("USER-410-001", "회원 탈퇴 철회 가능 기간이 만료되었습니다.", HttpStatus.GONE),
    JWT_MISSING("JWT-401-002", "토큰이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    JWT_EXPIRED("JWT-401-001", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_SIGNATURE("JWT-401-002", "잘못된 JWT 서명입니다.", HttpStatus.UNAUTHORIZED),
    MALFORMED_JWT("JWT-401-003", "잘못된 형식의 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT("JWT-401-004", "지원하지 않는 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    LOGGED_OUT_TOKEN("JWT-401-005", "로그아웃된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH-403-001", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    AUTH_REQUIRED("AUTH-401-001", "인증이 필요한 요청입니다.", HttpStatus.UNAUTHORIZED),
    BOARD_NOT_FOUND("BOARD_404-001", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND("FILE_404-001", "존재하지 않는 파일입니다.", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE("FILE-400-001", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    PLAN_NOT_FOUND("PLAN_404-001", "존재하지 않는 계획입니다.", HttpStatus.NOT_FOUND),
    TIMER_NOT_FOUND("TIMER_404-001", "존재하지 않는 타이머입니다.", HttpStatus.NOT_FOUND),

    TIMER_ALREADY_STATE("TIMER-400-001", "이미 요청하신 상태의 타이머 입니다.", HttpStatus.BAD_REQUEST),
    TIMER_NOT_RUNNING("TIMER-400-002", "실행 중인 타이머가 아닙니다.", HttpStatus.BAD_REQUEST),
    TIMER_RUNNING("TIMER-400-003", "실행 중인 타이머가 있습니다. 정지 및 종료 후 다시 시도해 주세요.", HttpStatus.BAD_REQUEST),
    TIMER_ENDED("TIMER-400-004", "종료된 타이머는 상태 변경이 불가합니다.", HttpStatus.BAD_REQUEST),
    TIMER_PLAN_COMPLETED("TIMER-400-005", "완료 처리된 계획은 설정 및 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    TIMER_ALREADY_EXISTS("TIMER-400-006", "해당 계획의 타이머가 이미 존재합니다.", HttpStatus.BAD_REQUEST),
    TIMER_RESET_FOR_COMPLETED_PLAN("TIMER-400-007", "타이머의 계획이 완료 상태일 경우 초기화가 불가합니다.", HttpStatus.BAD_REQUEST),
    TIMER_CATEGORY_PLAN_MISMATCH("TIMER-400-008", "입력된 카테고리가 계획 카테고리와 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND("CATEGORY-404-001", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_MODIFIABLE("CATEGORY-4000-001", "해당 카테고리는 변경 및 삭제가 불가합니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_DUPLICATE("CATEGORY-409-001", "동일한 카테고리명이 존재합니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_SEPARATED("CATEGORY-400-001", "카테고리는 콤마(,)로 구분되어야 합니다.", HttpStatus.BAD_REQUEST),
    NOTI_NOT_FOUND("NOTIFICATION-404-001", "존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("REQ-400-001", "잘못된 요청입니다. 요청 정보를 다시 한 번 확인해 주세요.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("SYS-500-001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_ERROR("SYS-500-002", "Redis 작업 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SSE_ERROR("SYS-500-003", "SSE 연결이 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
