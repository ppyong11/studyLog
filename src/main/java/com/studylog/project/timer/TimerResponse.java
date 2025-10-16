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
    private Long timerId;
    @Schema(description = "타이머명", example = "테스트")
    private String timerName;
    @Schema(description = "플랜명", example = "공부 계획")
    private String planName;
    @Schema(description = "플랜 URL", example = "plans/{plan_id}")
    private String planUrl;
    @Schema(description = "카테고리명", example = "공부")
    private String categoryName;
    @Schema(description = "타이머 생성일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    @Schema(description = "타이머 경과시간", example = "1200")
    private Long elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;
}
