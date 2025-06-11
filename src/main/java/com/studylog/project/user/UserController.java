package com.studylog.project.user;

import com.studylog.project.global.exception.LogoutFaildException;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.jwt.JwtService;
import com.studylog.project.jwt.JwtToken;
import com.studylog.project.jwt.JwtTokenProvider;
import com.studylog.project.mail.MailRequest;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.mail.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study-log")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final MailService mailService;
    private final JwtService jwtService; //회원 정보 비교 및 토큰 발급
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/sign-in/check-info")
    public ResponseEntity<ApiResponse> check(@RequestParam(required = false) String id,
                                             @RequestParam(required = false) String nickname) {
        if (id != null && nickname != null) { //두 필드 모두 들어올 경우
            return ResponseEntity.badRequest().body(new ApiResponse(false, "아이디와 닉네임 동시 검사가 불가합니다."));
        }
        else if (id != null) {//id 중복 확인
            if(!id.matches("^[a-zA-Z0-9]{4,12}$")){ //영어, 숫자, 6~20자 사이의 아이디만 가능
                return ResponseEntity.badRequest().body(new ApiResponse(false, "아이디는 4~12자 영문 또는 숫자여야 합니다."));
            }
            else{ //유효성 검사 통과
                boolean available = !userService.existsId(id); //id 중복 시 1 반환, available은 0이 됨
                return available ? ResponseEntity.ok(new ApiResponse(true, "사용 가능한 아이디입니다."))
                        : ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "이미 사용 중인 아이디입니다."));
            }
        } else if (nickname != null) {//닉네임 중복 확인
            if(!nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$")){ //한글, 영어, 숫자, 2~10자 사이의 닉네임만 가능
                return ResponseEntity.badRequest().body(new ApiResponse(false, "닉네임은 2~10자 한글, 영어, 숫자여야 합니다"));
            }
            else{
                boolean available = !userService.existsNickname(nickname); //중복 확인
                return available ? ResponseEntity.ok(new ApiResponse(true, "사용 가능한 닉네임입니다."))
                        : ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "이미 사용 중인 닉네임입니다."));
            }
        } else { // 아무 값도 없는 경우, 상태코드 400
            return ResponseEntity.badRequest().body(new ApiResponse(false, "아이디나 닉네임이 입력되지 않았습니다."));
        }
    }

    //이메일 인증 코드 발송
    @PostMapping("/sign-in/send-email-code")
    public ResponseEntity<ApiResponse> sendEmailCode(@RequestBody @Valid MailRequest reqeust) {
        log.info("1");
        String email= reqeust.getEmail(); //유효성 검사 후 받은 이메일 string형 변환
        if (userService.existsEmail(email)){
            log.info("3");
            return ResponseEntity.status(HttpStatus.CONFLICT).
                    body(new ApiResponse(false, "이미 사용 중인 이메일입니다."));
        }
        mailService.sendEmailCode(email); //랜덤 코드 생성 후 이메일 발송, 에러 시 핸들러가 처리
        return ResponseEntity.ok(new ApiResponse(true, "사용 가능한 이메일입니다. 이메일 발송 완료"));
        }

    @PostMapping("/sign-in/verify-email-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestBody @Valid MailRequest reqeust) {
        if(reqeust.getCode() == null || reqeust.getCode().isBlank()){ //code 입력 안 했을 때
            return ResponseEntity.badRequest().body(new ApiResponse(false, "인증 코드를 입력하세요."));
        }
        mailService.verifyEmailCode(reqeust.getEmail(), reqeust.getCode()); //Redis 값 비교, 오류 시 핸들러 처리
        return ResponseEntity.ok(new ApiResponse(true, "이메일 인증 완료")); //mailService에서 문제 없으면 처리
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse> singIn(@RequestBody @Valid SignInRequest signInRequest) {
        userService.register(signInRequest);
        return ResponseEntity.ok(new ApiResponse(true, "회원가입 되었습니다."));
    }

    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse> logIn(@RequestBody @Valid LogInRequest request) {
        JwtToken jwtToken= jwtService.logIn(request.getId(), request.getPw()); //예외 발생 시 아래 로직 실행 X
        log.info("요청- ID: {}, PW: {}", request.getId(), request.getPw());
        log.info("AccessToken: {}, RefreshToken: {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());
        System.out.println("redis rt 저장 완료: ");
        String nickname= userService.getNickname(request);
        return ResponseEntity.ok(new ApiResponse(true, String.format("%s 님, 반갑습니다. ☺️", nickname)));
    }

    @PostMapping("/log-out")
    @Operation(summary= "로그아웃", security = @SecurityRequirement(name= "bearerAuth"))
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        //토큰 없는 경우엔 필터에서 다 걸러서 여기까지 안 옴 -> 토큰은 항상 있음! 인증된 객체라는 뜻 (authenticated)
        String token= jwtTokenProvider.resolveToken(request); //토큰 추출
        jwtService.saveBlacklistToken(token); //액세스 토큰 저장

        return ResponseEntity.ok(new ApiResponse(true, "로그아웃 처리되었습니다."));
    }

    @PatchMapping("/member/change-pw")
    @Operation(summary = "비밀번호 변경", security = @SecurityRequirement(name= "bearerAuth"))
    public ResponseEntity<ApiResponse> updatePW(@AuthenticationPrincipal CustomUserDetail user,
                                                @RequestBody @Valid UpdatePwRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changePw(user, request);
        return ResponseEntity.ok(new ApiResponse(true, "비밀번호가 변경되었습니다."));
    }

    @PatchMapping("/member/change-nickname")
    @Operation(summary = "닉네임 변경", security = @SecurityRequirement(name= "bearerAuth"))
    public ResponseEntity<ApiResponse> updateNickname(@AuthenticationPrincipal CustomUserDetail user,
                                                @RequestBody @Valid UpdateNicknameRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changeNickname(user, request);
        return ResponseEntity.ok(new ApiResponse(true, "닉네임이 변경되었습니다."));
    }

    @PostMapping("/member/withdraw")
    @Operation(summary = "회원탈퇴", security = @SecurityRequirement(name= "bearerAuth"))
    public ResponseEntity<ApiResponse> withdraw(){

        return ResponseEntity.ok(new ApiResponse(true, "회원탈퇴 되었습니다."));
    }
}
