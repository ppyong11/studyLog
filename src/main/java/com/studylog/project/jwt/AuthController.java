package com.studylog.project.jwt;

import com.studylog.project.global.exception.JwtException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.user.LogInRequest;
import com.studylog.project.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/study-log")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    //토큰 관련 처리 담당
    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse> logIn(@RequestBody @Valid LogInRequest request, HttpServletResponse response) {
        userService.validateAndRestoreUser(request); //회원 검증 및 탈퇴 복구 처리
        JwtToken jwtToken = jwtService.logIn(request.getId(), request.getPw()); //예외 발생 시 아래 로직 실행 X
        log.info("요청- ID: {}, PW: {}", request.getId(), request.getPw());
        log.info("AccessToken: {}, RefreshToken: {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());
        String nickname = userService.getNickname(request);
        ResponseCookie accessCookie= ResponseCookie.from("access_token", jwtToken.getAccessToken())
                .httpOnly(true)
                .secure(false) //아직 http
                .path("/")
                .sameSite("None")
                .maxAge(60*60) //1시간 후 만료
                .build();

        ResponseCookie refreshCookie= ResponseCookie.from("refresh_token", jwtToken.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/member/refresh") //재발급 경로
                .maxAge(7*24*60*60) //7일
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(new ApiResponse(200, true, String.format("%s 님, 반갑습니다. ☺️", nickname)));
    }

    @PostMapping("/log-out")
    @Operation(summary = "로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request,
                                              @AuthenticationPrincipal CustomUserDetail user) {
        //토큰 없는 경우엔 필터에서 다 걸러서 여기까지 안 옴 -> 토큰은 항상 있음! 인증된 객체라는 뜻 (authenticated)
        String token = jwtTokenProvider.resolveToken(request); //토큰 추출
        jwtService.saveBlacklistToken(token, user.getUsername()); //액세스 토큰 저장


        return ResponseEntity.ok(new ApiResponse(200, true, "로그아웃 처리되었습니다."));
    }

    @PostMapping("/member/withdraw")
    @Operation(summary = "회원탈퇴", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> withdraw(HttpServletRequest request,
                                                @AuthenticationPrincipal CustomUserDetail user) {
        //자동 로그아웃 + user 속성 바꾸기
        String token = jwtTokenProvider.resolveToken(request); //토큰 추출

        jwtService.saveBlacklistToken(token, user.getUsername()); //액세스 토큰 저장
        //로그아웃 확인
        log.info("cont " + redisTemplate.opsForValue().get("AT:" + token));
        log.info("cont" + redisTemplate.opsForValue().get("RT:" + user.getUsername()));
        userService.withdraw(user);
        return ResponseEntity.ok(new ApiResponse(200, true, "회원탈퇴 되었습니다."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    public ResponseEntity<ApiResponse> refreshAccessToken(HttpServletRequest request) {
        String refreshToken= extractCookie(request, "refresh_token");
        if(refreshToken == null) { //쿠키에 토큰 X
            throw new JwtException("재발급 검증에 필요한 토큰이 없습니다.");
        }
        if(!jwtTokenProvider.validateToken(refreshToken)) { //토큰 O, 검증 실패
            throw new JwtException("유효하지 않은 토큰입니다.");
        }
        //토큰 O, 검증 완료
        String useId= jwtTokenProvider.get(refreshToken)
    }

    //쿠키에서 토큰 출력
    private String extractCookie(HttpServletRequest request, String name) {
        if(request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            log.info("쿠키 꺼냄: {}", cookie);
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
