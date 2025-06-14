package com.studylog.project.global.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtException extends AuthenticationException {
    public JwtException(String message) {
        super(message);
    }
}
