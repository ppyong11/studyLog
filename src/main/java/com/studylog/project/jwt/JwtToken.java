package com.studylog.project.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class JwtToken {
    private String grantType; //인증 타입 (Bearer 등)
    private String accessToken;
    private String refreshToken;
}
