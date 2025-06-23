package com.studylog.project.jwt;

import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenProvider(@Value("${jwt.secret}")String secretKey, UserRepository userRepository, RedisTemplate<String, String> redisTemplate) {
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        this.key= Keys.hmacShaKeyFor(keyBytes);
        this.userRepository = userRepository;
        this.redisTemplate= redisTemplate;
    }

    //유저 정보로 Token 생성
    public JwtToken createToken(Authentication authentication) {
        //권한 가져오기
        String authority = authentication.getAuthorities().stream()
                .findFirst() //단일 권한
                .map(GrantedAuthority::getAuthority) //Collection<SimpleGranted~ > 타입의 권한을 문자열로 반환
                .orElseThrow(() -> new IllegalStateException("권한 정보가 없습니다."));

        long now= (new Date().getTime()); //현재 시간 저장

        //Access Token 생성
        Date accessTokenExpire= new Date(now + (15*60*1000)); //현재 시간 + 60분(ms)
        String accessToken= Jwts.builder()
                .setSubject(authentication.getName()) //사용자 이름 (토큰 주인)
                .claim("auth", authority) //사용자 권한 정보
                .setExpiration(accessTokenExpire) //토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) //비밀키로 서명
                .compact(); //토큰 문자열 생성

        //Refresh Token 생성 (액세스 토큰보다 김)
        String refreshToken= Jwts.builder()
                .setExpiration(new Date(now + (7*24*60*60*1000))) //현재 시간 + 7일(ms)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build(); //토큰 dto 생성
    }

    //토큰 정보 꺼내서 검증 객체 만듦
    public Authentication getAuthentication(String accessToken) {
        //복호화 및 claim 받음 (유효기간 지나도 받음)
        Claims claims = parseClaims(accessToken);

        if(claims.get("auth") == null){ //권한 없는 경우
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        String role= claims.get("auth", String.class); //ROlE_USER or RILE_ADMIN
        Collection<? extends  GrantedAuthority> authority= List.of(new SimpleGrantedAuthority(role));

        //UserDetails 객체 생성 후 Authentication 반환
        UserEntity userEntity= userRepository.findById(claims.getSubject())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("[%s]에 해당하는 회원을 찾을 수 없습니다.", claims.getSubject())));
        CustomUserDetail principal= new CustomUserDetail(userEntity);
        return new UsernamePasswordAuthenticationToken(principal, "", authority); //Authentication 객체
    }

    //토큰 정보 검증 메서드
    public Boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key) //비밀키 설정 (서명 검증용)
                    .build()
                    .parseClaimsJws(token); //실제 토큰 파싱 및 서명 검증 수행
            if (Boolean.TRUE.equals(redisTemplate.hasKey("AT:"+token))) {
                //유효 토큰 + 블랙리스트 저장= 로그아웃 토큰
                log.info("로그아웃 처리된 회원의 요청입니다.");
                return false;
            }
            return true;
        }catch (io.jsonwebtoken.security.SignatureException e){
            log.info("Invalid JWT signature: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e ) { //서명 검증 실패 or JWT 형식 잘못된 경우
            log.info("Invalid JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) { //유효기간 만료
            log.info("Expired JWT Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { //지원하지 않는 형식의 JWT
            log.info("Unsupported JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) { //null or 빈 문자열 토큰
            log.info("JWT claims string is empty: {}", e.getMessage());
        }
        //jwt 관련 에러는 로그만 띄우고 security filter chain에서 인증 관련 에러 뜨게끔 처리
        return false;
    }

    //jwt 토큰 복호화 후 claim 가져옴
    public Claims parseClaims(String token) {
        try{ //복호화 & claim 리턴
            return Jwts.parserBuilder() //파서 생성 준비
                    .setSigningKey(key) //서명 검증에 쓸 비밀키
                    .build() //JwtParser 객체 생성
                    .parseClaimsJws(token) //토큰 파싱 + 서명 검증 + Base64 디코딩 (예외 가능성 O)
                    .getBody(); //토큰 안의 payload(claims) 꺼냄
        } catch (ExpiredJwtException e) { //만료 토큰이라도 claim 반환
            return e.getClaims();
        }
    }

    //헤더에서 토큰 정보 추출
    public String resolveAccessToken(HttpServletRequest request) {
        Cookie[] cookies= request.getCookies(); //쿠키 꺼내가
        String token= null;

        if(cookies != null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("access_token")){
                    token= cookie.getValue(); //액세스 토큰 꺼내기
                    break; //있으면 for문 종료 -> return문 바로 감
                }
            }
        }
        return token; //없으면 null 반환
    }
}
