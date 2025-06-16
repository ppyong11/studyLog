package com.studylog.project.jwt;

import com.studylog.project.global.exception.LogoutFaildException;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

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
        saveToken("RT:" + id, rt, null, null); //리프레시 토큰 저장 (id -> 토큰)
        saveToken("RT:" + rt, rt, id, null); //양방향 저장 (토큰 -> id)
        return jwtToken;
    }

    public void saveToken(String key, String token, String userId, String state) {
        Claims claims= jwtTokenProvider.parseClaims(token);
        Date now= new Date();
        Date expiration= claims.getExpiration();
        long TTL= expiration.getTime() - now.getTime();

        if (state == null) {
            //리프레시 저장
            try {
                if (userId != null){
                    //키값- RT: 토큰, 값- userId
                    redisTemplate.opsForValue().set(key, userId, TTL, TimeUnit.MILLISECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, token, TTL, TimeUnit.MILLISECONDS);
                }
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
        saveToken("AT:"+ token, token, null, "로그아웃"); //액세스 저장
        String refreshToken= redisTemplate.opsForValue().get("RT:"+ userId);
        //accessToken 검증 후라서 보안 문제 약함 + 리프레시 바로 삭제
        redisTemplate.delete("RT:"+ userId); //리프레시 토큰 삭제 (강제 무효화)
        redisTemplate.delete("RT:" + refreshToken); //토큰에 저장된 id 삭제
        log.info("로그아웃 확인 {}, {}", redisTemplate.opsForValue().get("RT: "+userId), redisTemplate.opsForValue().get("RT: "+refreshToken));
    }

    //토큰 재발급
    //redis에 토큰 회전 및 쿠키에 재발급한 토큰들 내려주기
    public JwtToken createNewToken(String userId){
        UserEntity userEntity= userRepository.findById(userId) //userId 유니크 필드라 조회 문제 X
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
        //인증 객체 임의 생성
        List<GrantedAuthority> authorities= List.of(new SimpleGrantedAuthority(userEntity.getRole()? "ROLE_USER" : "ROLE_ADMIN"));
        Authentication authentication= new UsernamePasswordAuthenticationToken(userEntity.getId(), null, authorities);
        JwtToken newToken= jwtTokenProvider.createToken(authentication);
        return newToken;
    }

    //쿠키 생성
    public String createCookie(String name, String token, String path, long expTime){
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(expTime) //1시간 후 만료
                .build().toString();

    }
    //쿠키 삭제
    public String deleteCookie(String name, String path){
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path(path) //생성할 때 넣은 path로 설
                .sameSite("None")
                .maxAge(0) //바로 만료
                .build().toString();
    }

}
