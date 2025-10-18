package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Slf4j
public class TimerDetailResponse {
    @Schema(description = "타이머 id", example = "1")
    private Long timerId;
    @Schema(description = "타이머명", example = "테스트")
    private String timerName;
    @Schema(description = "계획명", example = "공부 계획")
    private String planName;
    @Schema(description = "계획 url", example = "plans/{id}")
    private String planUrl;
    @Schema(description = "카테고리 id", example = "5")
    private Long categoryId;
    @Schema(description = "타이머 생성일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    @Schema(description = "타이머 시작일자", example = "2025-07-12 21:00:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @Schema(description = "타이머 종료일자", example = "null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @Schema(description = "타이머 정지일자", example = "2025-07-12 21:20:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @Schema(description = "타이머 경과시간", example = "1200")
    private String elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;

    public static TimerDetailResponse toDto(TimerEntity timer, String formatElapsed) {
        return new TimerDetailResponse(timer.getId(), timer.getName(),
                timer.getPlan() == null? null: timer.getPlan().getName(),
                timer.getPlan() == null? null: "plans/" + timer.getPlan().getId(),
                timer.getCategory().getId(), timer.getCreateAt(),
                timer.getStartAt() ,timer.getEndAt(), timer.getPauseAt(),
                formatElapsed, timer.getStatus());
    }
}
