package com.studylog.project.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        if (requestURI.equals("/api/login")) {
            chain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveAccessToken(request);

        // 쿠키에 없으면 쿼리 파라미터에서 꺼내기 (sendBeacon용 *창 꺼지면 타이머 종료)
        if (token == null) {
            token = request.getParameter("token");
            log.info("쿼리 파라미터 token: {}", token);
        }

        log.info("JWT token: {}", token);
        if (token != null) {
            jwtTokenProvider.validateToken(token);
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
