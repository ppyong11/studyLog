package com.studylog.project.plan;

import java.util.List;

public record PlansForTimerSummary(
        List<PlansForTimerResponse> plans,
        Long totalItems
) {
}
