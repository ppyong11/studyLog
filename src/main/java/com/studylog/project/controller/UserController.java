package com.studylog.project.controller;


import com.studylog.project.dto.response.ApiResponse;
import org.apache.catalina.User;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.studylog.project.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class UserController {
    UserService userService;

    @GetMapping("/check-info")
    public ResponseEntity<ApiResponse> check(@RequestParam(required = false) String id,
                                             @RequestParam(required = false) String nickname) {
        if (id != null) {//id 중복 확인
            boolean available = !userService.existsId(id); //id 중복 시 1 반환, available은 0이 됨
            return available ? ResponseEntity.ok(new ApiResponse(true, "사용 가능한 아이디입니다."))
                    : ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "이미 사용 중인 아이디입니다."));
        } else if (nickname != null) {//닉네임 중복 확인
            boolean available = !userService.existsNickname(nickname); //중복 확인
            return available ? ResponseEntity.ok(new ApiResponse(true, "사용 가능한 닉네임입니다."))
                    : ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "이미 사용 중인 닉네임입니다."));
        } else { // 아무 값도 없는 경우, 상태코드 400
            return ResponseEntity.badRequest().body(new ApiResponse(false, "아이디나 닉네임이 입력되지 않았습니다."));
        }
    }

    @PostMapping("/send-email-code")
    public ResponseEntity<ApiResponse> sendEmailCode(@RequestParam String email) {
            if (userService.existsEmail(email)){
                return ResponseEntity.status(HttpStatus.CONFLICT).
                        body(new ApiResponse(false, "이미 사용 중인 이메일입니다."));
            }
            userService.sendVerificationCode(email); //랜덤 코드 생성 후 이메일 발송
            return ResponseEntity.ok(new ApiResponse(true, "사용 가능한 이메일입니다. 이메일 발송 완료"));
        }

    @PostMapping("/verify-email-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String email,
                                                  @RequestParam String code){
        boolean result= userService.verifyCode(email, code); //Redis 값 비교
        return result ? ResponseEntity.ok(new ApiResponse(true, "이메일 인증 성공"))
                : ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "이메일 인증 실패"));
    }
}
