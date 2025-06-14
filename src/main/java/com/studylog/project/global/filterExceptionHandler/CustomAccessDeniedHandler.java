package com.studylog.project.global.filterExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import java.io.IOException;

//인증 O, 권한 X 상황
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        log.info("denied Handler 호출 완");
        HttpStatus status = HttpStatus.FORBIDDEN;

        ApiResponse apiResponse= new ApiResponse(status.value(), false, "접근 권한이 없습니다.");
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");

        String responseBody=objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(responseBody);
    }
}
