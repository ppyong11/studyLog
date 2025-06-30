package com.studylog.project.plan;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PlanUpdateRequest {
    private String planName;
    private Long category;

}
