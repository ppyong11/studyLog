package com.studylog.project.plan;

import com.studylog.project.user.UserEntity;

import java.time.LocalDate;
import java.util.List;

public interface PlanRepositoryCustom {

    PlanSummary getPlanSummaryByFilter(UserEntity user, LocalDate startDate, LocalDate endDate, List<Long> categoryIds,
                                      String keyword, Boolean status, List<String> sort, int page);
    PlanSummary findTodayPlans(UserEntity user, LocalDate today, int page);

    List<PlanResponse> getCalendarPlans(LocalDate startDate, LocalDate endDate, UserEntity user);
    PlansForTimerSummary getPlansForTimer(LocalDate startDate, LocalDate endDate, String keyword,
                                           String sort, int page, UserEntity user);
}
