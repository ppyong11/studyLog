package com.studylog.project.global.filterException;

import org.springframework.security.core.AuthenticationException;

public class TokenBlacklistedException extends AuthenticationException {
    public TokenBlacklistedException(String message) {
        super(message);
    }
}
