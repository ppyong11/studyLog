package com.studylog.project.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TimerResponse {
    private Long timerId;
    private String timerName;
    //수정창 들어가면 드롭다운 전체 조회됨
    private String planName;
    private String categoryName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime pauseAt;
    private int elapsedSeconds;
}
