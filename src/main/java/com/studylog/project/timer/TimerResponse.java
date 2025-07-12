package com.studylog.project.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class TimerResponse{
    private Long timerId;
    private String timerName;
    private String planName;
    private String categoryName;
    private LocalDate createDate;
    private Long elapsed;
    private TimerStatus status;

    public static TimerResponse toDto(TimerEntity timer) {
        return new TimerResponse(timer.getId(), timer.getTimerName(), timer.getPlan().getPlan_name(),
                timer.getCategory().getName(), timer.getCreateDate(), timer.getElapsed(),
                timer.getStatus());
    }
}
