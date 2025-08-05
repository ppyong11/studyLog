package com.studylog.project.Lap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.timer.TimerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class LapResponse {
    @Schema(description = "랩 id", example = "6")
    private Long id;
    @Schema(description = "타이머 id", example = "1")
    private Long timerId;
    @Schema(description = "랩명", example = "랩 테스트")
    private String lapName;
    @Schema(description = "랩 생성일", example = "2025-07-12", type = "string")
    private LocalDate createDate;
    @Schema(description = "랩 시작일자", example = "2025-07-12 21:10:17", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @Schema(description = "랩 정지일자", example = "null", type="null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @Schema(description = "랩 종료일자", example = "2025-07-12 21:12:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @Schema(description = "랩 경과시간", example = "103")
    private Long elapsed;
    @Schema(description = "랩 상태", example = "ENDED")
    private TimerStatus status;

    public static LapResponse toDto(LapEntity lap){
        return new LapResponse(lap.getId(), lap.getTimer().getId(),lap.getLapName(),
                lap.getCreateDate(), lap.getStartAt(), lap.getPauseAt(),
                lap.getEndAt(), lap.getElapsed(), lap.getStatus());
    }
}
