package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PlanDetailResponse {
    private List<PlanResponse> planList;
    private long achievedPlan;
    private long totalPlan;
    private String rate;
    private String message;
    private String totalStudyTime;

    public static PlanDetailResponse toDto(List<PlanResponse> response, long achieved, long total,
                                           double rate, String message, String totalStudyTime){
        return new PlanDetailResponse(response, achieved, total, String.format("%.1f%%", rate), message, totalStudyTime);
    }
}
