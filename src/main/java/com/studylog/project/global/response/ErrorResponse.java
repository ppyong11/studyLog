package com.studylog.project.global.response;

import com.studylog.project.global.exception.ErrorCode;

public record ErrorResponse(String code, String message) {
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }
}
