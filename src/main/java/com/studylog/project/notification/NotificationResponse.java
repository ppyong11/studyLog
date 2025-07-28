package com.studylog.project.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private LocalDateTime alertDate;
    private String url;
    private boolean isRead;
    private boolean isDeleted;

    public static NotificationResponse toDto(NotificationEntity noti){
        return new NotificationResponse(noti.getId(), noti.getTitle(), noti.getContent(),
                noti.getAlertAt(), noti.getUrl(), noti.isRead(), noti.isDeleted());
    }
}
