package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class CalenderPlanResponse {
    private Long planId;
    private String planName;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isComplete;
}
