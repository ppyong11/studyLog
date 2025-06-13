package com.studylog.project.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum JwtErrorCode {
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 필요한 요청입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
