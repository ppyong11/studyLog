package com.studylog.project.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ScrollPlanResponse {
    private List<PlanResponse> plans;
    @Schema(description = "달성 계획 개수", example = "1")
    private long achieved;
    @Schema(description = "전체 계획 개수", example = "2")
    private long total;
    @Schema(description = "달성률", example = "50.0%")
    private String rate;
    @Schema(description = "총 공부 시간", example = "00:50:00")
    private String totalStudyTime;
    private int currentPage;
    boolean hasNext;

    public static ScrollPlanResponse toDto(List<PlanResponse> response, long achieved, long total,
                                           double rate, String totalStudyTime,
                                           int page, boolean hasNext){
        return new ScrollPlanResponse(response, achieved, total, String.format("%.1f%%", rate), totalStudyTime,
                page, hasNext);
    }
}