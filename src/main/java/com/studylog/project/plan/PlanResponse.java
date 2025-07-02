package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class PlanResponse {
    private Long planId;
    private String planName;
    private String category;
    private LocalDate planStart;
    private LocalDate planEnd;
    private Integer planMinutes;
    private boolean planStatus;
}
