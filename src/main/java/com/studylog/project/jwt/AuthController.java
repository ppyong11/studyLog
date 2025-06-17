package com.studylog.project.jwt;

import com.studylog.project.global.exception.JwtException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.user.LogInRequest;
import com.studylog.project.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        String accessCookie= jwtService.createCookie("access_token",jwtToken.getAccessToken(),
                "/", 60*60); //60분

        String refreshCookie= jwtService.createCookie("refresh_token", jwtToken.getRefreshToken(),
                "/study-log/refresh", 7*24*60*60); //7일

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);
        return ResponseEntity.ok(new ApiResponse(200, true, String.format("%s 님, 반갑습니다. ☺️", nickname)));
    }

    @PostMapping("/log-out")
    @Operation(summary = "로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> logout(@CookieValue(name="access_token")String accessToken,
                                              HttpServletResponse response,
                                              @AuthenticationPrincipal CustomUserDetail user) {
        //토큰 없는 경우엔 필터에서 다 걸러서 여기까지 안 옴 -> 토큰은 항상 있음! 인증된 객체라는 뜻 (authenticated)
        //로그아웃  시 액세스 토큰 필요해서 파라미터 받기 (컨트롤러에서 필요없으면 필터에서만 검증하면 됨)
        jwtService.saveBlacklistToken(accessToken, user.getUsername()); //액세스 토큰 저장

        String deleteAccessCookie= jwtService.deleteCookie("access_token", "/");
        String deleteRefreshCookie= jwtService.deleteCookie("refresh_token", "/study-log/refresh");
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie);

        return ResponseEntity.ok(new ApiResponse(200, true, "로그아웃 처리되었습니다."));
    }

    @PostMapping("/member/withdraw")
    @Operation(summary = "회원탈퇴", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> withdraw(@CookieValue(name="access_token")String accessToken,
                                                HttpServletResponse response,
                                                @AuthenticationPrincipal CustomUserDetail user) {
        //자동 로그아웃 + user 속성 바꾸기
        jwtService.saveBlacklistToken(accessToken, user.getUsername()); //액세스 토큰 저장
        String deleteAccessCookie= jwtService.deleteCookie("access_token", "/");
        String deleteRefreshCookie= jwtService.deleteCookie("refresh_token", "/study-log/refresh");
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie);

        userService.withdraw(user);
        return ResponseEntity.ok(new ApiResponse(200, true, "회원탈퇴 되었습니다."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    public ResponseEntity<ApiResponse> refreshAccessToken(@CookieValue(name="refresh_token", required = false)String refreshToken,
                                                          HttpServletResponse response) {
        //permitAll 경로라 필터에서 쿠키 있든 없든 컨트롤러 호출 O
        //컨트롤러에서 RT 써야 해서 파라미터로 꺼냄
        log.info("refresh token: " + refreshToken);
        if(refreshToken == null) { //쿠키에 토큰 X
            throw new JwtException("재발급 검증에 필요한 토큰이 없습니다.");
        }
        if(!jwtTokenProvider.validateToken(refreshToken)) { //토큰 O, 검증 실패
            throw new JwtException("유효하지 않은 토큰입니다.");
        }
        //토큰 O, 검증 완료
        String userId= redisTemplate.opsForValue().get("RT:" + refreshToken); //userId (long) 타입으로 안 넣음
        if (userId == null) { //서버에 등록 안 된 토큰
            log.warn("로그아웃 및 탈퇴한 회원이거나 이전에 사용한 리프레시 토큰으로 access token 재발급 실패");
            throw new JwtException("유효하지 않은 토큰입니다.");
        }
        log.info("재발급 전 refresh: {}", refreshToken);
        JwtToken newToken= jwtService.createNewToken(userId);
        String accessCookie= jwtService.createCookie("access_token", newToken.getAccessToken(),
                "/", 60*60);
        log.info("재발급 후 access: {}", newToken.getAccessToken());
        String refreshCookie= jwtService.createCookie("refresh_token", newToken.getRefreshToken(),
                "/study-log/refresh", 7*24*60*60);
        log.info("재발급 후 refresh: {}", newToken.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);
        return ResponseEntity.ok(new ApiResponse(200, true, "로그인이 연장되었습니다."));
    }
}
