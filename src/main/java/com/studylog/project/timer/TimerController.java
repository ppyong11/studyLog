package com.studylog.project.timer;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.user.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("study-log/timers")
public class TimerController {
    private final TimerService timerService;

    @GetMapping("{timerId}")
    public ResponseEntity<TimerResponse> getTimer(@PathVariable("timerId") Long id,
                                                  @AuthenticationPrincipal UserEntity user) {
        TimerResponse response = timerService.getTimer(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<TimerResponse> createTimer(@Valid @RequestBody TimerRequest request,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.createTimer(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{timerId}/start")
    public ResponseEntity<TimerResponse> startTimer(@PathVariable("timerId") Long id,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.startTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{timerId}/pause")
    public ResponseEntity<TimerResponse> pauseTimer(@PathVariable("timerId") Long id,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.pauseTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{timerId}/end")
    public ResponseEntity<TimerResponse> endTimer(@PathVariable("timerId") Long id,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.endTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}")
    public ResponseEntity<TimerResponse> updateTimer(@PathVariable("timerId") Long id,
                                                     @Valid @RequestBody TimerRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.updateTimer(id, request,user.getUser());
        return ResponseEntity.ok(response);
    }

    //경과 시간 리셋
    @PatchMapping("{timerId}/reset")
    public ResponseEntity<TimerResponse> resetTimer(@PathVariable("timerId") Long id,
                                                    @AuthenticationPrincipal CustomUserDetail user) {
        TimerResponse response= timerService.resetTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    //타이머 계획 or 카테고리 업데이트 시, 계획/카테고리 대조 잘하기 계획 잇는데 카테고리 다른 거로 바꿀 수 X
    @DeleteMapping("/{timerId}")
    public ResponseEntity<ApiResponse> deleteTimer(@PathVariable("timerId") Long id,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        timerService.deleteTimer(id, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "타이머가 삭제되었습니다."));
    }
}
