package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class PlansForTimerResponse {
    private Long planId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
}
