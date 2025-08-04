package com.studylog.project.Lap;

import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.timer.TimerDetailResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timers")
@Slf4j
@RequiredArgsConstructor
@Tag(name="Lap", description = "Lap API, 모든 요청 access token 필요")
public class LapController {
    private final LapService lapService;

    @PostMapping("/{timerId}/laps")
    public ResponseEntity<TimerDetailResponse> createLap(@PathVariable Long timerId,
                                                         @Valid @RequestBody LapRequest request,
                                                         @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= lapService.createLap(timerId, request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}/laps/{lapId}/start")
    public ResponseEntity<TimerDetailResponse> startLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(lapService.startLap(timerId, lapId, user.getUser()));
    }

    @PatchMapping("/{timerId}/laps/{lapId}/pause")
    public ResponseEntity<TimerDetailResponse> pauseLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(lapService.pauseLap(timerId, lapId, user.getUser()));
    }

    @PatchMapping("/{timerId}/laps/{lapId}/end")
    public ResponseEntity<TimerDetailResponse> endLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(lapService.endLap(timerId, lapId, user.getUser()));
    }

    @PatchMapping("/{timerId}/laps/{lapId}")
    public ResponseEntity<TimerDetailResponse> updateLap(@PathVariable Long timerId,
                                                         @PathVariable Long lapId,
                                                         @Valid @RequestBody LapRequest request,
                                                         @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= lapService.updateLap(timerId, lapId, request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{timerId}/laps/{lapId}")
    public ResponseEntity<TimerDetailResponse> deleteLap(@PathVariable Long timerId,
                                                         @PathVariable Long lapId,
                                                         @AuthenticationPrincipal CustomUserDetail user){
        TimerDetailResponse response= lapService.deleteLap(timerId, lapId, user.getUser());
        return ResponseEntity.ok(response);
    }
}
