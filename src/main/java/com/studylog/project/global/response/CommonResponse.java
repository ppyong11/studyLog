package com.studylog.project.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "API Response")
public class CommonResponse {
    private boolean success;
    private String message;

    public CommonResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
