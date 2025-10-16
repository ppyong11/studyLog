package com.studylog.project.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class NotificationResponse {
    @Schema(description = "알림 id")
    private Long notificationId;
    @Schema(description = "알림 제목")
    private String title;
    @Schema(description = "알림 내용")
    private String content;
    @Schema(description = "알림 발생 일자")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime alertAt;
    @Schema(description = "알림 클릭 시 이동되는 url")
    private String url;
    @Schema(description = "알림 읽음 체크")
    private boolean isRead;
}
