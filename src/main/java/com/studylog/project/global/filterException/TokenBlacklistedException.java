package com.studylog.project.global.filterException;

import com.studylog.project.global.exception.JwtException;
import org.springframework.security.core.AuthenticationException;

public class TokenBlacklistedException extends JwtException {
    public TokenBlacklistedException(String message) {
        super(message);
    }
}
