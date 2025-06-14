package com.studylog.project.global.response;

import lombok.Getter;

@Getter
public class ApiResponse {
    private boolean success;
    private String message;
    private int status;

    public ApiResponse(int status, boolean success, String message) {
        this.status = status;
        this.success = success;
        this.message = message;
    }
}
