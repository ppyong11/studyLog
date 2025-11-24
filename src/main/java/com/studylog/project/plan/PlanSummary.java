package com.studylog.project.plan;

import java.util.List;

public record PlanSummary(
        List<PlanResponse> plans,
        Long totalCount,
        Long achivedCount,
        Long totalStudyTime
){}
