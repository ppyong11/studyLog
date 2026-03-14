package com.studylog.project.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LinkedPlanDto {
    private Long id;
    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
}
