package com.studylog.project.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtToken logIn(String id, String password) {
        //1. 로그인 정보를 기반으로 Authentication 객체 생성
        //이때 인증 여부를 확인하는 authenticated 는 false
        UsernamePasswordAuthenticationToken authenticationToken=
                new UsernamePasswordAuthenticationToken(id, password);
        //2.실제 검증 - .authenticate() 통해서 요청된 유저에 대한 검증 진행
        //authenticate 메서드가 실행될 때 CustomUserDetailsService의 loadUserByUsername 메서드 실행
        Authentication authentication= authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //3. 인증 정보를 기반으로 JWT 토큰 생성
        JwtToken jwtToken= jwtTokenProvider.createToken(authentication);
        String rt= jwtToken.getRefreshToken(); //검증 후에 토큰 만드니까 오류 X
        saveToken("RT:" + id, rt); //리프레시 토큰 저장
        log.info("redis rt 저장 완료: "+redisTemplate.opsForValue().get("RT:"+ id));
        return jwtToken;
    }

    public void saveToken(String key, String token) {
        Claims claims= jwtTokenProvider.parseClaims(token);
        Date now= new Date();
        Date expiration= claims.getExpiration();
        long TTL= expiration.getTime() - now.getTime();
        try {
            redisTemplate.opsForValue().set(key, token, TTL, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()); //잡아서 컨트롤러에 던짐
        }
        log.info("redis rt 저장 완료: "+redisTemplate.opsForValue().get(key));
        System.out.println("redis rt 저장 완료: "+redisTemplate.opsForValue().get(key));
    }
    //token으로 회원 id 찾고... 저장

}
