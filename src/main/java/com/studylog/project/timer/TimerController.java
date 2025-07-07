package com.studylog.project.timer;

import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("study-log/timer")
public class TimerController {
    private final TimerService timerService;

    @PostMapping("")
    public ResponseEntity<TimerResponse> startTimer(@Valid @RequestBody TimerStartReqeust request,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.setTimer(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    //타이머 계획 or 카테고리 업데이트 시, 계획/카테고리 대조 잘하기 계획 잇는데 카테고리 다른 거로 바꿀 수 X

}
