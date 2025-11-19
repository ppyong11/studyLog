package com.studylog.project.jwt;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.SuccessResponse;
import com.studylog.project.user.LogInRequest;
import com.studylog.project.user.UserResponse;
import com.studylog.project.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name="User-auth", description = "JWT 관련 API, 로그인 제외 모든 요청 access token 필요")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    @Operation(summary= "로그인", description = "로그인 (jwt 토큰 발급), 회원탈퇴 철회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                example = "{\n  \"success\": true,\n  \"message\": \"[닉네임] 님, 반갑습니다. ☺️ \"\n}"))),
        @ApiResponse(responseCode = "400", description = "로그인 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"아이디 또는 비밀번호가 일치하지 않습니다. / 회원탈퇴 철회 기간이 지나 복구가 불가합니다.\"\n}"
            )))
    })
    //토큰 관련 처리 담당
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<UserResponse>> logIn(@RequestBody @Valid LogInRequest request, HttpServletResponse response) {
        userService.validateAndRestoreUser(request); //회원 검증 및 탈퇴 복구 처리
        JwtToken jwtToken = jwtService.logIn(request.id(), request.pw()); //예외 발생 시 아래 로직 실행 X
        log.info("요청- ID: {}, PW: {}", request.id(), request.pw());
        log.info("AccessToken: {}, RefreshToken: {}", jwtToken.accessToken(), jwtToken.refreshToken());

        UserResponse userResponse = userService.getCurrentUser(request.id());
        String accessCookie= jwtService.createCookie("access_token",jwtToken.accessToken(),
                "/", 30*60); //30분 동안 쿠키 보냄 (TTL이랑 오차 거의 없음)

        String refreshCookie= jwtService.createCookie("refresh_token", jwtToken.refreshToken(),
                "/api/refresh", 7*24*60*60); //7일 동안 쿠키 보냄

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);

        return ResponseEntity.ok(SuccessResponse.of("로그인 성공", userResponse));
    }


    //hidden: Swagger에서 안 보임
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<SuccessResponse<Void>> logout(@Parameter(hidden=true) @CookieValue(name="access_token")String accessToken,
                                                 HttpServletResponse response,
                                                 @AuthenticationPrincipal CustomUserDetail user) {
        //토큰 없는 경우엔 필터에서 다 걸러서 여기까지 안 옴 -> 토큰은 항상 있음! 인증된 객체라는 뜻 (authenticated)
        //로그아웃  시 액세스 토큰 필요해서 파라미터 받기 (컨트롤러에서 필요없으면 필터에서만 검증하면 됨)
        jwtService.saveBlacklistToken(user, accessToken); //액세스 토큰 저장

        String deleteAccessCookie= jwtService.deleteCookie("access_token", "/");
        String deleteRefreshCookie= jwtService.deleteCookie("refresh_token", "/api/refresh");
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie);

        return ResponseEntity.ok(SuccessResponse.of("로그아웃 처리되었습니다."));
    }

    @PostMapping("/member/withdraw")
    @Operation(summary = "회원탈퇴 (softDelete - 7일간 회원 정보 보관)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"회원탈퇴 처리되었습니다.\"\n}"))),
        @ApiResponse(responseCode = "400", description = "타이머로 인한 회원탈퇴 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"실행 중인 타이머를 종료한 후 다시 시도해 주세요.\"\n}"
            ))),
        @ApiResponse(responseCode = "401", description = "토큰 검증 실패로 인한 회원탈퇴 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"로그인이 필요한 요청입니다.\"\n}"
            )))
    })
    public ResponseEntity<SuccessResponse<Void>> withdraw(@Parameter(hidden=true) @CookieValue(name="access_token")String accessToken,
                                                   HttpServletResponse response,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        //자동 로그아웃 + user 속성 바꾸기
        jwtService.saveBlacklistToken(user, accessToken); //액세스 토큰 저장
        String deleteAccessCookie= jwtService.deleteCookie("access_token", "/");
        String deleteRefreshCookie= jwtService.deleteCookie("refresh_token", "/api/refresh");
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie);

        userService.withdraw(user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("회원탈퇴 처리되었습니다."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
        content= @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": true,\n  \"message\": \"로그인이 연장되었습니다.\"\n}"))),
    @ApiResponse(responseCode = "401", description = "토큰 검증 실패로 인한 재발급 실패",
        content = @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"액세스 토큰 X- 로그인이 필요한 요청입니다. / 리프레시 토큰 X- 재발급 검증에 필요한 토큰이 없습니다." +
                        " / 블랙리스트 처리된 리프레시 토큰- 유효하지 않은 토큰입니다.\"\n}"
        )))
    })
    public ResponseEntity<SuccessResponse<Void>> refreshAccessToken(@Parameter(hidden=true) @CookieValue(name="access_token", required = false)String accessToken,
                                                             @Parameter(hidden=true) @CookieValue(name="refresh_token", required = false)String refreshToken,
                                                             @AuthenticationPrincipal CustomUserDetail user,
                                                             HttpServletResponse response) {
        //permitAll 경로라 필터에서 쿠키 있든 없든 컨트롤러 호출 O
        //컨트롤러에서 RT 써야 해서 파라미터로 꺼냄
        if(refreshToken == null) { //쿠키에 토큰 X
            log.info("refresh 토큰 없음");
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }

        //토큰 O, 검증 완료 -> id 얻어옴
        String userId= redisTemplate.opsForValue().get("RT:" + refreshToken); //userId (long) 타입으로 안 넣음

        if (userId == null) { //서버에 등록 안 된 토큰
            log.warn("로그아웃 및 탈퇴한 회원이거나 이전에 사용한 리프레시 토큰입니다.");
            throw new CustomException(ErrorCode.JWT_EXPIRED);
        }

        JwtToken newToken= jwtService.createNewToken(userId, user, accessToken);
        String accessCookie= jwtService.createCookie("access_token", newToken.accessToken(),
                "/", 30*60);
        String refreshCookie= jwtService.createCookie("refresh_token", newToken.refreshToken(),
                "/api/refresh", 7*24*60*60);
        log.info("토큰 재발급 완료");
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);
        return ResponseEntity.ok(SuccessResponse.of("로그인이 연장되었습니다."));
    }
}
