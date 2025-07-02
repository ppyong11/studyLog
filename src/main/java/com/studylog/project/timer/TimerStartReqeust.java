package com.studylog.project.timer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class TimerStartReqeust {
    private Long plan; //null 가능
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long category; //필수
    @NotNull(message = "시작 시간은 필수 값입니다.")
    private LocalDateTime startAt;
}
