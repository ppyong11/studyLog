package com.studylog.project.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Response")
public record SuccessResponse<T>(
        String message,
        T data,
        Integer tokenExpiresIn
) {
    public static <T> SuccessResponse<T> of(String message, T data) {
        return new SuccessResponse<>(message, data, null);
    }

    public static SuccessResponse<Void> of(String message) {
        return new SuccessResponse<>(message, null, null);
    }

    // 로그인용
    public static <T> SuccessResponse<T> of(String message, T data, Integer expired) {
        return new SuccessResponse<>(message, data, expired);
    }

    // 토큰 재발급 성공용
    public static SuccessResponse<Void> of(String message, Integer expired) {
        return new SuccessResponse<>(message, null, expired);
    }
}
