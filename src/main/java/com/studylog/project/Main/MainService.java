package com.studylog.project.Main;

import com.studylog.project.plan.PlanDetailResponse;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanService;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
//메인 화면 데이터 조합용 서비스
public class MainService {
    private final UserService userService;
    private final PlanService planService;

    //유저 닉넴+다짐, 플랜 리스트(당일), 위클리 리스트+당성률 (정렬은 항상 asc, asc)
    public MainResponse buildMainPage(UserEntity user){
        UserEntity userEntity= userService.getUser(user);
        PlanDetailResponse todayPlans= planService.returnMainPage(user, LocalDate.now(), false);
        PlanDetailResponse weeklyPlans= planService.returnMainPage(user, LocalDate.now(), true);
        return MainResponse.toDto(userEntity, todayPlans, weeklyPlans);
    }
}
