package com.studylog.project.Lap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.timer.TimerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class LapResponse {
    private Long id;
    private Long timerId;
    private String lapName;
    private LocalDate createDate;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    private Long elapsed;
    private TimerStatus status;

    public static LapResponse toDto(LapEntity lap){
        return new LapResponse(lap.getId(), lap.getTimer().getId(),lap.getLapName(),
                lap.getCreateDate(), lap.getStartAt(), lap.getPauseAt(),
                lap.getEndAt(), lap.getElapsed(), lap.getStatus());
    }
}
