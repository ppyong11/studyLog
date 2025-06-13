package com.studylog.project.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CustomErrorSend {
    public static void handleException (HttpServletResponse response, JwtErrorCode errorCode,
                                        String message) throws IOException {
        ApiResponse apiResponse= new ApiResponse(false, errorCode.getMessage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}
