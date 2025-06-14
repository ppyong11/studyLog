package com.studylog.project.jwt;

import com.studylog.project.global.exception.LogoutFaildException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
        saveToken("RT:" + id, rt, null); //리프레시 토큰 저장
        return jwtToken;
    }

    public void saveToken(String key, String token, String state) {
        Claims claims= jwtTokenProvider.parseClaims(token);
        Date now= new Date();
        Date expiration= claims.getExpiration();
        long TTL= expiration.getTime() - now.getTime();

        if (state == null) {
            //리프레시 저장
            try {
                redisTemplate.opsForValue().set(key, token, TTL, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage()); //잡아서 에러 던짐
            }
            log.info("rt 저장 완료: "+redisTemplate.opsForValue().get(key));
        } else{
            //액세스 저장
            try {
                redisTemplate.opsForValue().set(key, state, TTL, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage()); //잡아서 에러 던짐
            }
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            String value = redisTemplate.opsForValue().get(key);
            log.info("value: {}, TTL: {}", value, ttl);
        }
    }

    //블랙리스트 저장 (로그아웃)
    public void saveBlacklistToken(String token, String userId){
        saveToken("AT:"+ token, token, "로그아웃"); //액세스 저장
        redisTemplate.delete("RT:"+ userId); //리프레시 토큰 삭제 (강제 무효화)
        log.info("로그아웃 확인 {}", redisTemplate.opsForValue().get("RT: "+userId));
    }

}
