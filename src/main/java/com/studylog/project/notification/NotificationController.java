package com.studylog.project.notification;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.getNotifications(user.getUser()));
    }

    @DeleteMapping("/")
    public ResponseEntity<ApiResponse> deleteAllNotifications(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(notificationService.deleteAllNotification(user.getUser()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetail user){
        notificationService.deleteNotification(id, user.getUser());
        return ResponseEntity.noContent().build(); //204 No Content (성공, 본문 없음)
    }

    //알림 모두 읽음 처리
    @PatchMapping("/")

    //알림 개별 읽음 처리
}
