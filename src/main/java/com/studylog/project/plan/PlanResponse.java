package com.studylog.project.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class PlanResponse {
    private Long planId;
    private String planName;
    private String memo;
    private String categoryName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minutes;
    private boolean status;

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
