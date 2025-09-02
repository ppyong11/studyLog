package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.Lap.LapResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class TimerDetailResponse {
    @Schema(description = "타이머 id", example = "1")
    private Long timerId;
    @Schema(description = "타이머명", example = "테스트")
    private String timerName;
    //수정창 들어가면 드롭다운 전체 조회됨
    @Schema(description = "계획 id", example = "5")
    private Long planId;
    @Schema(description = "계획명", example = "공부 계획")
    private String planName;
    @Schema(description = "계획 시작일자", example = "2025-07-12")
    private LocalDate planStartDate;
    @Schema(description = "계획 종료일자", example = "2025-07-12")
    private LocalDate planEndDate;
    @Schema(description = "계획 완료여부", example = "true")
    private Boolean planStatus;
    @Schema(description = "카테고리명", example = "공부")
    private String categoryName;
    @Schema(description = "타이머 생성일자", example = "2025-07-12")
    private LocalDate createDate;
    @Schema(description = "타이머 시작일자", example = "2025-07-12 21:00:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @Schema(description = "타이머 종료일자", example = "null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @Schema(description = "타이머 정지일자", example = "2025-07-12 21:20:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @Schema(description = "타이머 경과시간", example = "1200")
    private Long elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;
    private List<LapResponse> laps;

    public static TimerDetailResponse toDto(TimerEntity timer) {
        //Lazy라 getLaps 때 쿼리 나감 (하나의 타이머에 해당하는 랩 조회 후 컬렉션을 DTO로 변환)
        List<LapResponse> laps = timer.getLaps().stream()
                .map(LapResponse::toDto)
                .collect(Collectors.toList());

        return new TimerDetailResponse(timer.getId(), timer.getName(),
                timer.getPlan() == null? null: timer.getPlan().getId(),
                timer.getPlan() == null? null: timer.getPlan().getPlan_name(),
                timer.getPlan() == null? null: timer.getPlan().getStartDate(),
                timer.getPlan() == null? null: timer.getPlan().getEndDate(),
                timer.getPlan() == null? null: timer.getPlan().isStatus(),
                timer.getCategory().getName(), timer.getCreateDate(),
                timer.getStartAt() ,timer.getEndAt(), timer.getPauseAt(),
                timer.getElapsed(), timer.getStatus(), laps);
    }
}
