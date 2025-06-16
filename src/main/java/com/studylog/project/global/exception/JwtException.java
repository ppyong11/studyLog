package com.studylog.project.global.exception;

//authenticationException으로 해도 jwtFilter에서 터진 오류 entry point에서 못 잡음...
public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
}
