package com.studylog.project.notification;

import com.studylog.project.global.CommonValidator;
import com.studylog.project.global.response.ScrollResponse;
import com.studylog.project.global.response.SuccessResponse;
import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name="Notification", description = "알림 관련 API, 모든 요청 access token 필요")
public class NotificationController {
    private final NotificationService notificationService;
    @Operation(summary = "전체 알림 조회", description = "SSE를 통해 받은 알림 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "알림 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationResponse.class),
                    examples = @ExampleObject(
                            name = "NotificationResponse 예시",
                            summary = "성공 응답 예시",
                            value = """
            [
                {
                   "notificationId": 1,
                   "title": "[계획2-동기화로 알림] 계획이 목표 달성 시간을 채워 자동완료 처리되었어요. 🥳",
                   "content": "[동기화 알림 테스트]로 이동해서 타이머를 종료해 주세요.",
                   "alertDate": "25-07-29 01:20:00",
                   "read": false
                 },
                 {
                   "notificationId": 2,
                   "title": "[계획1] 계획이 목표 달성 시간을 채워 완료 처리되었어요.",
                   "content": "",
                   "alertDate": "25-07-29 01:27:48",
                   "read": false
                 }
            ]
            """
            ))),
            @ApiResponse(responseCode = "401", description = "조회 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\n  \"success\": false,\n  \"message\": \"로그인이 필요한 요청입니다.\"\n}")))
    })
    @GetMapping("")
    public ResponseEntity<ScrollResponse<NotificationResponse>> getAllNoti(@RequestParam(required = false) int page,
                                                                           @AuthenticationPrincipal CustomUserDetail user){
        CommonValidator.validatePage(page);
        return ResponseEntity.ok(notificationService.getAllNoti(page, user.getUserId()));
    }    @Operation(summary = "미확인 알림 개수 조회", description = "미확인 알림 개수 띄우는 API")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getUserId()));
    }



    @Operation(summary = "알림 전체 삭제")
    @DeleteMapping("/")
    public ResponseEntity<SuccessResponse<Void>> deleteAllNoti(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.deleteAllNoti(user.getUserId()));
    }

    @Operation(summary = "특정 알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoti(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetail user){
        notificationService.deleteNoti(id, user.getUserId());
        return ResponseEntity.noContent().build(); //204 No Content (성공, 본문 없음)
    }

    //알림 모두 읽음 처리
    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> readAllNoti(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.readAllNoti(user.getUserId()));
    }

    //알림 개별 읽음 처리
    @Operation(summary = "특정 알림 읽음 처리")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNoti(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetail user){
        notificationService.readNoti(id, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
