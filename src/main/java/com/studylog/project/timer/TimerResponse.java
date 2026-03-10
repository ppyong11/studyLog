package com.studylog.project.timer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.plan.LinkedPlanDto;
import com.studylog.project.plan.PlanEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TimerResponse{
    @Schema(description = "타이머 id", example = "1")
    private Long id;
    @Schema(description = "타이머명", example = "테스트")
    private String name;
    @Schema(description = "카테고리 id", example = "3")
    private Long categoryId;
    @Schema(description = "타이머 생성일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    @Schema(description = "타이머 시작일자", example = "2025-07-12 20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    @Schema(description = "타이머 종료일자", example = "null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @Schema(description = "타이머 정지일자", example = "2025-07-12 21:20:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseAt;
    @Schema(description = "타이머 경과시간", example = "1200")
    private long elapsed;
    @Schema(description = "타이머 상태", example = "PAUSED")
    private TimerStatus status;
    @Schema(description = "연결된 계획")
    private LinkedPlanDto connectedPlan;

    // 생성자 오버로딩
    public TimerResponse(Long id, String name, Long categoryId, LocalDateTime createAt,
                         LocalDateTime startAt, LocalDateTime endAt, LocalDateTime pauseAt,
                         Long elapsed, TimerStatus status, Long planId, String planName,
                         LocalDate planStartDate, LocalDate planEndDate) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.createAt = createAt;
        this.startAt = startAt;
        this.endAt = endAt;
        this.pauseAt = pauseAt;
        this.elapsed = elapsed;
        this.status = status;

        if (planId != null) {
            this.connectedPlan = new LinkedPlanDto(planId, planName, planStartDate, planEndDate);
        } else {
            this.connectedPlan = null;
        }
    }

    // 플랜 X
    public static TimerResponse toDto(TimerEntity timer) {
        return new TimerResponse(timer.getId(), timer.getName(), timer.getCategory().getId(), timer.getCreateAt(),
                timer.getStartAt(), timer.getEndAt(), timer.getPauseAt(), timer.getElapsed(), timer.getStatus(),
                null

        );
    }

    //플랜 O
    public static TimerResponse toDto(TimerEntity timer, PlanEntity plan) {
        return new TimerResponse(timer.getId(), timer.getName(), timer.getCategory().getId(), timer.getCreateAt(),
                timer.getStartAt(), timer.getEndAt(), timer.getPauseAt(), timer.getElapsed(), timer.getStatus(),
                new LinkedPlanDto(plan.getId(), plan.getName(), plan.getStartDate(), plan.getEndDate())
        );
    }
}
