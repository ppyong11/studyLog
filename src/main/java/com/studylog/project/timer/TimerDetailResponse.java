package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.Lap.LapResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class TimerDetailResponse {
    private Long timerId;
    private String timerName;
    //수정창 들어가면 드롭다운 전체 조회됨
    private String planName;
    private String categoryName;
    private LocalDate createDate;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime restartAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    private Long elapsed;
    private TimerStatus status;
    private List<LapResponse> laps;

    public static TimerDetailResponse toDto(TimerEntity timer) {
        List<LapResponse> laps = timer.getLaps().stream()
                .map(LapResponse::toDto)
                .collect(Collectors.toList());

        return new TimerDetailResponse(timer.getId(), timer.getTimerName(),
                timer.getPlan() == null? null: timer.getPlan().getPlan_name(),
                timer.getCategory().getName(), timer.getCreateDate(),
                timer.getStartAt(), timer.getRestartAt(),timer.getEndAt(), timer.getPauseAt(),
                timer.getElapsed(), timer.getStatus(), laps);
    }
}
