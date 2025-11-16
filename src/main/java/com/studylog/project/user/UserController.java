package com.studylog.project.user;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.mail.MailRequest;
import com.studylog.project.global.response.SuccessResponse;
import com.studylog.project.mail.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name="User", description = "User API, 일부 요청 access token 필요")
public class UserController {
    private final UserService userService;
    private final MailService mailService;

    @Operation(summary = "유저 닉네임, 다짐 반환")
    @GetMapping("/member/user-info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(userService.getUserInfo(user.getUser()));
    }

    @Operation(summary= "아이디 중복 확인")
    @GetMapping("/signup/check-id")
    public ResponseEntity<SuccessResponse<Void>> checkId(@Valid @ModelAttribute IdRequest request) {
        userService.existsId(request.id());
        return ResponseEntity.ok(SuccessResponse.of("사용 가능한 아이디입니다."));
    }

    @Operation(summary= "닉네임 중복 확인")
    @GetMapping("/signup/check-nickname")
    public ResponseEntity<SuccessResponse<Void>> checkNickname(@Valid @ModelAttribute NicknameRequest request) {
        userService.existsNickname(request.nickname());
        return ResponseEntity.ok(SuccessResponse.of("사용 가능한 닉네임입니다."));
    }


    @Operation(summary= "이메일 인증 코드 발송")
    //이메일 인증 코드 발송
    @ApiResponses(value = {
            @ApiResponse (responseCode = "200", description = "이메일 코드 발송 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"사용 가능한 이메일입니다. 이메일 발송 완료\"\n}"))),
            @ApiResponse (responseCode = "400", description = "중복 이메일",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"이미 사용 중인 이메일입니다.\"\n}"
            )))
    })
    @PostMapping("/signup/send-email-code")
    public ResponseEntity<SuccessResponse<Void>> sendEmailCode(@RequestBody @Valid MailRequest reqeust) {
        String email = reqeust.email(); //유효성 검사 후 받은 이메일 string형 변환
        userService.existsEmail(email); // 이미 가입한 이메일일 경우
        mailService.existsRedis(email); // 이미 인증 완료된 메일일 경우
        mailService.sendEmailCode(email); //랜덤 코드 생성 후 이메일 발송, 에러 시 핸들러가 처리
        return ResponseEntity.ok(SuccessResponse.of("인증 메일이 전송되었습니다."));
    }

    @Operation(summary= "이메일 인증 코드 검증")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "200", description = "성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
            example = "{\n  \"success\": true,\n  \"message\": \"이메일이 인증되었습니다.\"\n}"))),
            @ApiResponse (responseCode = "400", description = "이메일 유효성 검증 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
            example = "{\n  \"success\": false,\n  \"message\": \"인증 코드를 입력하세요. / 인증 코드가 만료되었습니다. / 인증 코드가 일치하지 않습니다.\"\n}"
            )))
    })
    @PostMapping("/signup/verify-email-code")
    public ResponseEntity<SuccessResponse<Void>> verifyCode(@RequestBody @Valid MailRequest reqeust) {
        if (reqeust.code() == null || reqeust.code().isBlank()) { //code 입력 안 했을 때
            throw new CustomException(ErrorCode.MAIL_CODE_REQUIRED);
        }
        mailService.verifyEmailCode(reqeust.email(), reqeust.code()); //Redis 값 비교, 오류 시 핸들러 처리
        return ResponseEntity.ok(SuccessResponse.of("이메일이 인증되었습니다.\n이 인증은 10분 동안 유효하니 시간 내에 회원가입을 완료해 주세요.")); //mailService에서 문제 없으면 처리
    }

    @Operation(summary= "회원가입")
    @ApiResponses(value = {
        @ApiResponse (responseCode = "200", description = "회원가입 성공",
                content= @Content(mediaType = "application/json",
                        schema = @Schema(
                                example = "{\n  \"success\": true,\n  \"message\": \"회원가입 되었습니다.\"\n}"))),
        @ApiResponse (responseCode = "400", description = "회원가입 실패 (중복 아이디/닉네임/이메일, 인증 만료된 이메일)",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(
                                example = "{\n  \"success\": false,\n  \"message\": \"이미 가입된 회원입니다. / 인증 세션이 만료됐거나 인증된 메일이 아닙니다.\"\n}"
                        )))
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Void>> signUp(@RequestBody @Valid SignUpRequest request) {
        userService.register(request);
        return ResponseEntity.ok(SuccessResponse.of("회원가입 되었습니다."));
    }

    @GetMapping("/member")
    @Operation(summary = "유저 정보 반환")
    public ResponseEntity<UserResponse> getMemberInfo(@AuthenticationPrincipal CustomUserDetail user) {
        log.info("유저 정보 반환");

        return ResponseEntity.ok(UserResponse.of(userService.getUser(user.getUser(), ErrorCode.USER_NOT_FOUND)));
    }

    @PatchMapping("/member/change-pw")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<SuccessResponse<Void>> updatePW(@AuthenticationPrincipal CustomUserDetail user,
                                                   @RequestBody @Valid UpdatePwRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changePw(user.getUser(), request);
        return ResponseEntity.ok(SuccessResponse.of("비밀번호가 변경되었습니다."));
    }

    @PatchMapping("/member/change-nickname")
    @Operation(summary = "닉네임 변경")
    public ResponseEntity<SuccessResponse<Void>> updateNickname(@AuthenticationPrincipal CustomUserDetail user,
                                                         @RequestBody @Valid NicknameRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changeNickname(user.getUser(), request.nickname());
        log.info(user.getUsername());
        return ResponseEntity.ok(SuccessResponse.of("닉네임이 변경되었습니다."));
    }

    @Operation(summary = "다짐 변경")
    @PatchMapping("/member/change-resolution")
    public ResponseEntity<SuccessResponse<Void>> updateResolution(@Valid @RequestBody UpdateResolutionReqeust reqeust,
                                                   @AuthenticationPrincipal CustomUserDetail user){
        userService.updateResolution(reqeust.resolution(), user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("다짐이 변경되었습니다."));
    }
}
