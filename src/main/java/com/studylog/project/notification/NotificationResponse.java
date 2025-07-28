package com.studylog.project.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime alertDate;
    private String url;
    private boolean isRead;

    public static NotificationResponse toDto(NotificationEntity noti){
        return new NotificationResponse(noti.getId(), noti.getTitle(), noti.getContent(),
                noti.getAlertAt(), noti.getUrl(), noti.isRead());
    }
}
