package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class PlanResponse {
    private Long planId;
    private String planName;
    private LocalDate planStart;
    private LocalDate planEnd;
    private int planMinutes;
    private boolean planStatus;
}
