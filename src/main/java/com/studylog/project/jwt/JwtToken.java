package com.studylog.project.jwt;

import lombok.Builder;

@Builder
public record JwtToken(
        String grantType, // 인증 타입 (Bearer 등)
        String accessToken,
        String refreshToken
) {}
