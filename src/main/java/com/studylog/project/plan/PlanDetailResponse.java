package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PlanDetailResponse {
    private List<PlanResponse> planResponse;
    private long achievedPlan;
    private long totalPlan;
    private double rate;
    private String message;

    public static PlanDetailResponse toDto(List<PlanResponse> response, long achieved, long total,
                                           Double rate, String message){
        return new PlanDetailResponse(response, achieved, total, rate, message);
    }
}
