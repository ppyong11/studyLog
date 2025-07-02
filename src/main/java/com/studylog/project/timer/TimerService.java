package com.studylog.project.timer;

import com.studylog.project.plan.PlanRepository;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final PlanRepository planRepository;

    public void setTimer(TimerStartReqeust request, UserEntity user) {
        //유저 검증
        if (request.getPlan() != null){
            planRepository.ex
        }
    }
}
