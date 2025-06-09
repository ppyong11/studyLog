package com.studylog.project.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}")String secretKey) {
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        this.key= Keys.hmacShaKeyFor(keyBytes);
    }

    //유저 정보로 Token 생성
    public JwtToken createToken(Authentication authentication) {
        //권한 가져오기
        String authority = authentication.getAuthorities().stream()
                .findFirst() //단일 권한
                .map(GrantedAuthority::getAuthority) //Collection<SimpleGranted~ > 타입의 권한을 문자열로 반환
                .orElseThrow(() -> new IllegalStateException("권한 정보가 없습니다."));

        long now= (new Date().getTime()); //현재 시간 저장

        //jti 생성
        String jti= UUID.randomUUID().toString();

        //Access Token 생성
        Date accessTokenExpire= new Date(now + (60*60*1000)); //현재 시간 + 60분(ms)
        String accessToken= Jwts.builder()
                .setSubject(authentication.getName()) //사용자 이름 (토큰 주인)
                .claim("auth", authority) //사용자 권한 정보
                .claim("jti", jti)
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
        //UserDetails: 인터페이스, User: UserDetails 구현 클래스
        UserDetails principal= new User(claims.getSubject(), "", authority);
        return new UsernamePasswordAuthenticationToken(principal, "", authority); //Authentication 객체
    }

    //토큰 정보 검증 메서드
    public Boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key) //비밀키 설정 (서명 검증용)
                    .build()
                    .parseClaimsJws(token); //실제 토큰 파싱 및 서명 검증 수행
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
        return false; //캐치 들어가면 fasle 던짐
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
    public String resolveToken(HttpServletRequest request) {
        String bearerToken= request.getHeader("Authorization");
        log.info("토큰 추출: {}", bearerToken);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
