package com.studylog.project.sse;

import com.studylog.project.board.BoardDetailResponse;
import com.studylog.project.global.response.CommonResponse;
import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/sse")
@Tag(name="SSE", description = "SSE API, 모든 요청 access token 필요")
public class SseController {
    private final SseEmitterService sseEmitterService;

    //구독 상태 체크
    @Operation(summary = "로그인한 유저가 SSE 구독 중인지 조회", security = @SecurityRequirement(name= "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"구독된 알림 채널이 있습니다.\"\n}"))),
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"구독된 알림 채널이 없습니다.\"\n}")))
    })
    @GetMapping("/subscribe/status")
    public ResponseEntity<CommonResponse> isSubscribe(@AuthenticationPrincipal CustomUserDetail user) {
        if (!sseEmitterService.isSubscribe(user.getUser()))
            return ResponseEntity.ok(new CommonResponse(true, "구독된 알림 채널이 없습니다."));
        return ResponseEntity.ok(new CommonResponse(true, "구독된 알림 채널이 있습니다."));
    }

    //구독
    @Operation(summary = "SSE 구독", security = @SecurityRequirement(name= "bearerAuth"))
    @GetMapping(value= "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetail user) {
        //로그인 후에 구독 요청해서 인증 객체 있음
        return sseEmitterService.subscribe(user.getUser());
    }
}
