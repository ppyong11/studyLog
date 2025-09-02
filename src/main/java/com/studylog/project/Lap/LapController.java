package com.studylog.project.Lap;

import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.timer.TimerDetailResponse;
import com.studylog.project.timer.TimerService;
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
@RequestMapping("/api/timers")
@Slf4j
@RequiredArgsConstructor
@Tag(name="Lap", description = "Lap API, 모든 요청 access token 필요")
public class LapController {
    private final TimerService timerService;

    @Operation(summary = "랩 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "랩 등록 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = LapResponse.class))),
        @ApiResponse(responseCode = "400", description = "랩 등록 실패",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"종료 타이머에 생성 시- 종료된 타이머는 랩 생성이 불가합니다. / 동일 랩명 지정- 해당 랩명이 존재합니다.\"\n}"))),
        @ApiResponse(responseCode = "404", description = "랩 등록 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 타이머입니다.\"\n}")))
    })
    @PostMapping("/{timerId}/laps")
    public ResponseEntity<TimerDetailResponse> createLap(@PathVariable Long timerId,
                                                         @Valid @RequestBody LapRequest request,
                                                         @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.createLap(timerId, request, user.getUser());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "랩 시작", description = "실행 중인 타이머일 경우에만 랩 실행 가능, 이미 실행 중인 랩 있을 경우 실행 불가, 종료된 랩은 재실행 불가")
    @PatchMapping("/{timerId}/laps/{lapId}/start")
    public ResponseEntity<TimerDetailResponse> startLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(timerService.startLap(timerId, lapId, user.getUser()));
    }

    @Operation(summary = "랩 중지", description = "종료된 랩이거나 실행 중인 랩이 아니면 중지 불가")
    @PatchMapping("/{timerId}/laps/{lapId}/pause")
    public ResponseEntity<TimerDetailResponse> pauseLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(timerService.pauseLap(timerId, lapId, user.getUser()));
    }

    @Operation(summary = "랩 종료", description = "이미 종료된 랩이거나 실행 중인 랩이 아니면 종료 불가")
    @PatchMapping("/{timerId}/laps/{lapId}/end")
    public ResponseEntity<TimerDetailResponse> endLap(@PathVariable Long timerId,
                                                        @PathVariable Long lapId,
                                                        @AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(timerService.endLap(timerId, lapId, user.getUser()));
    }

    @Operation(summary = "랩 수정", description = "랩명만 수정 가능")
    @PatchMapping("/{timerId}/laps/{lapId}")
    public ResponseEntity<TimerDetailResponse> updateLap(@PathVariable Long timerId,
                                                         @PathVariable Long lapId,
                                                         @Valid @RequestBody LapRequest request,
                                                         @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.updateLap(timerId, lapId, request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랩 삭제", description = "랩 삭제 시, 타이머 전체 기록에 영향 없음")
    @DeleteMapping("/{timerId}/laps/{lapId}")
    public ResponseEntity<TimerDetailResponse> deleteLap(@PathVariable Long timerId,
                                                         @PathVariable Long lapId,
                                                         @AuthenticationPrincipal CustomUserDetail user){
        TimerDetailResponse response= timerService.deleteLap(timerId, lapId, user.getUser());
        return ResponseEntity.ok(response);
    }
}
