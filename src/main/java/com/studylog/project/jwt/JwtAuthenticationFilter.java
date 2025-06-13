package com.studylog.project.jwt;

import ch.qos.logback.core.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.exception.JwtException;
import com.studylog.project.global.filterException.TokenBlacklistedException;
import com.studylog.project.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try{
            //1. 요청 헤더에서 JWT 토큰 추출
            String token= jwtTokenProvider.resolveToken(request);

            //2. 토큰 유효성 검사
            if (token != null && jwtTokenProvider.validateToken(token)) { //예외 처리 시 false 반환
                //인증 완료된 사용자니까 Authentication 객체 생성 & Context에 저장
                //SecurityContext: 현재 요청을 처리하는 사용자의 인증 정보 저장
                if (Boolean.TRUE.equals(redisTemplate.hasKey("AT:" + token))) { //로그아웃 처리된 토큰
                    log.info("JwtAuthenticationFilter 들어옴, 로그아웃 회원 예외 던짐");
                    throw new TokenBlacklistedException("로그아웃 처리된 회원의 요청입니다.");
                }
                Authentication authentication= jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response); //false 반환 시 예외 처리 X
        }catch (JwtException authException){
            String errorMessage = authException.getMessage(); //FORBIDDEN_REQUEST
            JwtErrorCode errorCode= JwtErrorCode.valueOf(errorMessage); //위 메시지에 해당하는 ENUM이 들어감

            CustomErrorSend.handleException(response, errorCode, errorMessage);
        }
         log.info("jwt필터 넘어감");
    }

    public static void setErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());
        ObjectMapper objectMapper = new ObjectMapper();

        ApiResponse apiResponse= new ApiResponse(false, errorCode.getMessage());
    }
}
