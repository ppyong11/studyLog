package com.studylog.project.plan;

import com.studylog.project.timer.LinkedTimerDto;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class PlanResponse {
    @Schema(description = "계획 id", example = "5")
    private Long id;
    @Schema(description = "계획명", example = "공부 계획")
    private String name;
    @Schema(description = "계획 메모", example = "챕터 5까지 풀기")
    private String memo;
    @Schema(description = "카테고리 id", example = "1")
    private Long categoryId;
    @Schema(description = "계획 시작일자", example = "2025-07-12")
    private LocalDate startDate;
    @Schema(description = "계획 종료일자", example = "2025-07-12")
    private LocalDate endDate;
    @Schema(description = "계획 목표 시간", example = "10")
    private Integer minutes;
    @Schema(description = "완료여부", example = "true")
    private Boolean completed;
    @Schema(description = "연결된 타이머")
    // 객체 기준으로 JSON 직렬화됨
    private LinkedTimerDto connectedTimer;

    //QueryDSL에서 쓰임
    public PlanResponse(Long id, String name, String memo, Long categoryId,
                        LocalDate startDate, LocalDate endDate, Integer minutes,
                        Boolean completed, Long timerId, String timerName, Long timerCategoryId,
                        LocalDateTime creatAt, LocalDateTime startAt, Long timerElapsed, TimerStatus status) {
        this.id = id;
        this.name = name;
        this.memo = memo;
        this.categoryId = categoryId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minutes = minutes;
        this.completed = completed;

        // 타이머 ID가 있을 때만 DTO 조립
        if (timerId != null) {
            this.connectedTimer = new LinkedTimerDto(timerId, timerName, categoryId, creatAt, startAt,timerElapsed, status);
        } else {
            this.connectedTimer = null;
        }
    }

    public static PlanResponse toDto(PlanEntity plan){
        //객체 생성 없이 바로 사용
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMemo(),
                plan.getCategory().getId(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getMinutes(),
                plan.isComplete(),
                null
        );
    }

    // 오버로딩
    public static PlanResponse toDto(PlanEntity plan, TimerEntity timer){
        //객체 생성 없이 바로 사용
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMemo(),
                plan.getCategory().getId(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getMinutes(),
                plan.isComplete(),
                new LinkedTimerDto(timer.getId(), timer.getName(), timer.getCategory().getId(), timer.getCreateAt(),
                        timer.getStartAt(), timer.getElapsed(), timer.getStatus())
        );
    }
}
