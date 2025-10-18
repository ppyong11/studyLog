package com.studylog.project.user;

import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.DuplicateException;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.mail.MailRequest;
import com.studylog.project.global.response.CommonResponse;
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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CommonResponse> checkId(@RequestParam(required = false) String id) {
        if (id == null) throw new BadRequestException("아이디를 입력해 주세요.");

        if (!id.matches("^[a-zA-Z0-9]{4,12}$")) { //영어, 숫자, 6~20자 사이의 아이디만 가능
            throw new BadRequestException("아이디는 4~12자 영문 또는 숫자여야 합니다.");
        } else { //유효성 검사 통과
            //id 중복 시 1 반환, available은 0이 됨
            if (!userService.existsId(id))
                return ResponseEntity.ok(new CommonResponse(true, "사용 가능한 아이디입니다."));
            else throw new DuplicateException("이미 사용 중인 아이디입니다.");
        }
    }
    @Operation(summary= "닉네임 중복 확인")
    @GetMapping("/signup/check-nickname")
    public ResponseEntity<CommonResponse> checkNickname(@RequestParam(required = false) String nickname) {
        if(nickname == null) throw new BadRequestException("닉네임을 입력해 주세요.");
        if (!nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$")) { //한글, 영어, 숫자, 2~10자 사이의 닉네임만 가능
            throw new BadRequestException("닉네임은 2~10자 한글, 영어, 숫자여야 합니다");
        } else {
            if(!userService.existsNickname(nickname))
                return ResponseEntity.ok(new CommonResponse(true, "사용 가능한 닉네임입니다."));
            else throw new DuplicateException("이미 사용 중인 닉네임입니다.");
        }
    }


    @Operation(summary= "이메일 인증 코드 발송")
    //이메일 인증 코드 발송
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 코드 발송 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"사용 가능한 이메일입니다. 이메일 발송 완료\"\n}"))),
            @ApiResponse(responseCode = "400", description = "중복 이메일",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"이미 사용 중인 이메일입니다.\"\n}"
            )))
    })
    @PostMapping("/signup/send-email-code")
    public ResponseEntity<CommonResponse> sendEmailCode(@RequestBody @Valid MailRequest reqeust) {
        String email = reqeust.getEmail(); //유효성 검사 후 받은 이메일 string형 변환
        if (userService.existsEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new CommonResponse( false, "이미 사용 중인 이메일입니다."));
        }
        mailService.sendEmailCode(email); //랜덤 코드 생성 후 이메일 발송, 에러 시 핸들러가 처리
        return ResponseEntity.ok(new CommonResponse( true, "사용 가능한 이메일입니다. 이메일 발송 완료"));
    }

    @Operation(summary= "이메일 인증 코드 검증")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
            example = "{\n  \"success\": true,\n  \"message\": \"이메일이 인증되었습니다.\"\n}"))),
            @ApiResponse(responseCode = "400", description = "이메일 유효성 검증 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
            example = "{\n  \"success\": false,\n  \"message\": \"인증 코드를 입력하세요. / 인증 코드가 만료되었습니다. / 인증 코드가 일치하지 않습니다.\"\n}"
            )))
    })
    @PostMapping("/signup/verify-email-code")
    public ResponseEntity<CommonResponse> verifyCode(@RequestBody @Valid MailRequest reqeust) {
        if (reqeust.getCode() == null || reqeust.getCode().isBlank()) { //code 입력 안 했을 때
            return ResponseEntity.badRequest().body(new CommonResponse( false, "인증 코드를 입력하세요."));
        }
        mailService.verifyEmailCode(reqeust.getEmail(), reqeust.getCode()); //Redis 값 비교, 오류 시 핸들러 처리
        return ResponseEntity.ok(new CommonResponse(true, "이메일이 인증되었습니다.")); //mailService에서 문제 없으면 처리
    }

    @Operation(summary= "회원가입")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공",
                content= @Content(mediaType = "application/json",
                        schema = @Schema(
                                example = "{\n  \"success\": true,\n  \"message\": \"회원가입 되었습니다.\"\n}"))),
        @ApiResponse(responseCode = "400", description = "회원가입 실패 (중복 아이디/닉네임/이메일, 인증 만료된 이메일)",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(
                                example = "{\n  \"success\": false,\n  \"message\": \"이미 가입된 회원입니다. / 인증 세션이 만료됐거나 인증된 메일이 아닙니다.\"\n}"
                        )))
    })
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse> signUp(@RequestBody @Valid SignInRequest signInRequest) {
        userService.register(signInRequest);
        return ResponseEntity.ok(new CommonResponse(true, "회원가입 되었습니다."));
    }

    @PatchMapping("/member/change-pw")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<CommonResponse> updatePW(@AuthenticationPrincipal CustomUserDetail user,
                                                   @RequestBody @Valid UpdatePwRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changePw(user, request);
        return ResponseEntity.ok(new CommonResponse(true, "비밀번호가 변경되었습니다."));
    }

    @PatchMapping("/member/change-nickname")
    @Operation(summary = "닉네임 변경")
    public ResponseEntity<CommonResponse> updateNickname(@AuthenticationPrincipal CustomUserDetail user,
                                                         @RequestBody @Valid UpdateNicknameRequest request) {
        //유효 토큰 및 로그인 상태 확인(redis) 필터에서 검증됨
        userService.changeNickname(user, request);
        log.info(user.getUsername());
        return ResponseEntity.ok(new CommonResponse(true, "닉네임이 변경되었습니다."));
    }

    @Operation(summary = "다짐 변경")
    @PatchMapping("/member/change-resolution")
    public ResponseEntity<String> updateResolution(@Valid @RequestBody UpdateResolutionReqeust reqeust,
                                                   @AuthenticationPrincipal CustomUserDetail user){
        String updated= userService.updateResolution(reqeust.getResolution(), user.getUser());
        return ResponseEntity.ok((updated));
    }
}
