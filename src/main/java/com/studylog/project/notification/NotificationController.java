package com.studylog.project.notification;

import com.studylog.project.global.response.CommonResponse;
import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name="Notification", description = "알림 관련 API, 모든 요청 access token 필요")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<List<NotificationResponse>> getAllNoti(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.getAllNoti(user.getUser()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getUser()));
    }

    @DeleteMapping("/")
    public ResponseEntity<CommonResponse> deleteAllNoti(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.deleteAllNoti(user.getUser()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoti(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetail user){
        notificationService.deleteNoti(id, user.getUser());
        return ResponseEntity.noContent().build(); //204 No Content (성공, 본문 없음)
    }

    //알림 모두 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<CommonResponse> readAllNoti(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.readAllNoti(user.getUser()));
    }

    //알림 개별 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNoti(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetail user){
        notificationService.readNoti(id, user.getUser());
        return ResponseEntity.noContent().build();
    }
}
