package com.studylog.project.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class PlanResponse {
    @Schema(description = "계획 id", example = "5")
    private Long planId;
    @Schema(description = "계획명", example = "공부 계획")
    private String name;
    @Schema(description = "계획 메모", example = "챕터 5까지 풀기")
    private String memo;
    @Schema(description = "카테고리명", example = "공부")
    private String categoryName;
    @Schema(description = "계획 시작일자", example = "2025-07-12")
    private LocalDate startDate;
    @Schema(description = "계획 종료일자", example = "2025-07-12")
    private LocalDate endDate;
    @Schema(description = "계획 목표 시간", example = "10")
    private Integer minutes;
    @Schema(description = " 완료여부", example = "true")
    private Boolean status;
    public static PlanResponse toDto(PlanEntity plan){
        //객체 생성 없이 바로 사용
        return new PlanResponse(
                plan.getId(),
                plan.getPlan_name(),
                plan.getPlan_memo(),
                plan.getCategory().getName(), //id to name
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getMinutes(),
                plan.isStatus()
        );
    }
}
