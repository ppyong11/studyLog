package com.studylog.project.Main;

import com.studylog.project.plan.PlanDetailResponse;
import com.studylog.project.plan.PlanResponse;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MainResponse {
    private String nickname;
    private String resolution;
    private PlanDetailResponse todayPlans;
    private PlanDetailResponse weeklyPlans;

    public static MainResponse toDto(UserEntity user, PlanDetailResponse todayPlans, PlanDetailResponse planDeatil){
        return new MainResponse(user.getNickname(), user.getResolution(), todayPlans, planDeatil);
    }
}
