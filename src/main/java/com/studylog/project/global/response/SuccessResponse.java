package com.studylog.project.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Response")
public record SuccessResponse<T>(
        String message,
        T data
) {
    public static <T> SuccessResponse<T> of(String message, T data) {
        return new SuccessResponse<>(message, data);
    }

    public static SuccessResponse<Void> of(String message) {
        return new SuccessResponse<>(message, null);
    }
}
