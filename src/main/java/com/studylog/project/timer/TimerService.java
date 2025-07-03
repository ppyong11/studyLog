package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.plan.PlanEntity;
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
    private final CategoryRepository categoryRepository;

    public TimerResponse setTimer(TimerStartReqeust request, UserEntity user) {
        //여기서 선언 안 하면 if-else문 안에서만 존재함
        PlanEntity plan= null;
        CategoryEntity category= null;
        TimerEntity timer= null;
        //유저 검증
        if (request.getPlan() != null){
            log.info("계획 입력됨");
            plan= planRepository.findByUserAndId(user, request.getPlan())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
            log.info("계획 검증 통과 O");
            if(!plan.getCategory().getId().equals(request.getCategory())){ //달라도 입력 카테고리 무시하긴 하지만 일관성을 위해 넣음
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
            }
            //계획에 딸린 카테고리로 타이머 카테고리도 설정됨
            timer= request.toEntity(user, plan, plan.getCategory());
        } else { //plan == null, 카테고리만 검색
            log.info("입력된 계획 없음");
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                            .orElseThrow(()-> new NotFoundException("존재하지 않는 카테고리입니다."));
            log.info("카테고리 검증 통과 O");
            timer= request.toEntity(user, null, category);
        }
        timerRepository.saveAndFlush(timer); //이때 timer에도 id 매핑됨 (AI 된 값)

        return new TimerResponse(
                timer.getId(), timer.getTimerName(),
                timer.getPlan() == null? null: timer.getPlan().getPlan_name(),
                timer.getCategory().getName(),timer.getStartAt(), timer.getEndAt(),
                timer.getPauseAt(), 0);
        }
    }

