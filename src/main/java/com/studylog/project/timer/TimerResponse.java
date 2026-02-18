package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TimerResponse{
    @Schema(description = "타이머 id", example = "1")
    private Long id;
    @Schema(description = "타이머명", example = "테스트")
    private String name;
    @Schema(description = "플랜 id", example = "12")
    private Long planId;
    @Schema(description = "플랜명", example = "계획명")
    private String planName;
    @Schema(description = "카테고리 id", example = "3")
    private Long categoryId;
    @Schema(description = "타이머 생성일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    @Schema(description = "타이머 시작일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @Schema(description = "타이머 종료일자", example = "null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @Schema(description = "타이머 정지일자", example = "2025-07-12 21:20:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @Schema(description = "타이머 경과시간", example = "1200")
    private long elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;

    public static TimerResponse toDto(TimerEntity timer) {
        return new TimerResponse(timer.getId(), timer.getName(), timer.getPlan().getId(),
                timer.getPlan().getName(), timer.getCategory().getId(), timer.getCreateAt(),
                timer.getStartAt(), timer.getEndAt(), timer.getPauseAt(), timer.getElapsed(), timer.getStatus()
        );
    }
}
