package com.studylog.project.global.filterExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
//토큰 없는 경우, security filter에서 에러 터지면 잡는 용도
//AuthenticationEntryPoint는 보통 401 에러 (인증 실패)
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException{
        log.info("entry 호출: 401 에러 발생");
        ErrorCode errorCode = ErrorCode.AUTH_REQUIRED;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json; charset=UTF-8");

        String responseBody= objectMapper.writeValueAsString(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
        response.getWriter().write(responseBody);
    }
}
