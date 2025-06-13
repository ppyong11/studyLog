package com.studylog.project.global.exception;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
}
