package com.studylog.project.global;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonThrow {
    private CommonThrow() {};

    public static void invalidRequest(String message) {
        log.info(message);
        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
}
