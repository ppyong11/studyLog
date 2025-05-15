package com.studylog.project.user;


import com.studylog.project.mail.MailErrorCode;
import com.studylog.project.mail.MailRequest;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.mail.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study-log")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MailService mailService;

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
    public ResponseEntity<ApiResponse> sendEmailCode(@RequestBody @Valid MailRequest emailDTO) {
        String email= emailDTO.getEmail(); //유효성 검사 후 받은 이메일 string형 변환
        if (userService.existsEmail(email)){
            return ResponseEntity.status(HttpStatus.CONFLICT).
                    body(new ApiResponse(false, "이미 사용 중인 이메일입니다."));
        }
        mailService.sendEmailCode(email); //랜덤 코드 생성 후 이메일 발송, 에러 시 핸들러가 처리
        return ResponseEntity.ok(new ApiResponse(true, "사용 가능한 이메일입니다. 이메일 발송 완료"));
        }

    @PostMapping("/sign-in/verify-email-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestBody @Valid MailRequest emailDTO) {
        if(emailDTO.getCode() == null || emailDTO.getCode().isBlank()){ //code 입력 안 했을 때
            return ResponseEntity.badRequest().body(new ApiResponse(false, "인증 코드를 입력하세요."));
        }
        mailService.verifyEmailCode(emailDTO.getEmail(), emailDTO.getCode()); //Redis 값 비교, 오류 시 핸들러 처리
        return ResponseEntity.ok(new ApiResponse(true, "이메일 인증 완료")); //mailService에서 문제 없으면 처리
    }

    @PostMapping("/sing-in")
    public ResponseEntity<ApiResponse> singIn(@RequestBody @Valid UserRequest userRequest) {
        userService.register(userRequest);
        return ResponseEntity.ok(new ApiResponse(true, "회원가입 되었습니다."));

    }
}
