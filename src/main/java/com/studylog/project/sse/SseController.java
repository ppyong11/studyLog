package com.studylog.project.sse;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("study-log/sse")
public class SseController {
    private final SseEmitterService sseEmitterService;

    //구독 상태 체크
    @GetMapping("/subscribe/status")
    public ResponseEntity<ApiResponse> isSubscribe(@AuthenticationPrincipal CustomUserDetail user) {
        if (!sseEmitterService.isSubscribe(user.getUser()))
            return ResponseEntity.ok(new ApiResponse(200, true, "구독된 알림 채널이 없습니다."));
        return ResponseEntity.ok(new ApiResponse(200, true, "구독된 알림 채널이 있습니다."));
    }
    //구독
    @GetMapping(value= "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetail user) {
        //로그인 후에 구독 요청해서 인증 객체 있음
        return sseEmitterService.subscribe(user.getUser());
    }
    //알림 보내기
    @PostMapping("/broadcast")
    public void broadcast(@AuthenticationPrincipal CustomUserDetail user) {}

}
