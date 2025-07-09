package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TimerResponse {
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
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime syncedAt; //비정상 종료 & 동기화 시간 띄울 때 활용
    private Long elapsedSeconds;

    public static TimerResponse toDto(TimerEntity timer) {
        return new TimerResponse(timer.getId(), timer.getTimerName(),
                timer.getPlan() == null? null: timer.getPlan().getPlan_name(),
                timer.getCategory().getName(), timer.getCreateDate(),
                timer.getStartAt(), timer.getRestartAt(),timer.getEndAt(), timer.getPauseAt(),
                timer.getSyncedAt(), timer.getElapsedSecond());
    }
}
