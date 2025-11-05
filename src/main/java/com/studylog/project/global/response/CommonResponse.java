package com.studylog.project.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "API Response")
public class CommonResponse<T> {
    private final boolean success;
    private final String message;
    private T data;

    public CommonResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
