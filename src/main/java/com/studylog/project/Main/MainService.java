package com.studylog.project.Main;

import com.studylog.project.notification.NotificationService;
import com.studylog.project.plan.ScrollPlanResponse;
import com.studylog.project.plan.PlanService;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
//메인 화면 데이터 조합용 서비스
public class MainService {
    private final PlanService planService;
    private final NotificationService notificationService;

    public MainUserInfoResponse getUserInfo(UserEntity user){
        return new MainUserInfoResponse(user.getNickname(), user.getResolution(),
                notificationService.getUnreadCount(user));
    }

    public MainPlansResponse<ScrollPlanResponse> getDailyPlans(UserEntity user, int page){
        ScrollPlanResponse response= planService.MainDailyPlans(user, LocalDate.now(), page);
        return new MainPlansResponse<>(response);
    }

    /*
    //유저 닉넴+다짐, 플랜 리스트(당일), 위클리 리스트+당성률 (정렬은 항상 asc, asc)
    public MainPlansResponse getWeeklyPlans(UserEntity user){

        return MainPlansResponse<>();
    }

    public MainPlansResponse getMonthlyPlans(UserEntity user){
        return MainPlansResponse<>();
    }*/
}
