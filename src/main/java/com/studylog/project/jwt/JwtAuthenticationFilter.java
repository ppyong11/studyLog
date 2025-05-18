package com.studylog.project.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //1. 요청 헤더에서 JWT 토큰 추출
        String token= resolveToken((HttpServletRequest) request);

        //2. 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {
            //인증 완료된 사용자니까 Authentication 객체 생성 & Context에 저장
            //SecurityContext: 현재 요청을 처리하는 사용자의 인증 정보 저장
            Authentication authentication= jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    //헤더에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken= request.getHeader("Authorization");
        log.info("토큰 추출: {}", bearerToken);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
