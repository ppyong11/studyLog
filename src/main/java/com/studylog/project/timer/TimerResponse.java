package com.studylog.project.timer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class TimerResponse{
    @Schema(description = "타이머 id", example = "1")
    private Long timerId;
    @Schema(description = "타이머명", example = "테스트")
    private String timerName;
    @Schema(description = "플랜명", example = "공부 계획")
    private String planName;
    @Schema(description = "카테고리명", example = "공부")
    private String categoryName;
    @Schema(description = "타이머 생성일자", example = "2025-07-12")
    private LocalDate createDate;
    @Schema(description = "타이머 경과시간", example = "1200")
    private Long elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;

    public static TimerResponse toDto(TimerEntity timer) {
        return new TimerResponse(timer.getId(), timer.getTimerName(),
                timer.getPlan() == null? null : timer.getPlan().getPlan_name(),
                timer.getCategory().getName(), timer.getCreateDate(), timer.getElapsed(),
                timer.getStatus());
    }
}
