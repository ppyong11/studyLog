package com.studylog.project.global.filterExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.response.CommonResponse;
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
        log.info("entry 호출 완");
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        CommonResponse commonResponse = new CommonResponse(false, "로그인이 필요한 요청입니다.");

        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");

        String responseBody= objectMapper.writeValueAsString(commonResponse);
        response.getWriter().write(responseBody);
    }
}
