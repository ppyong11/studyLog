package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;

    public List<PlanResponse> getPlans(UserEntity user) {
        List<PlanEntity> plans= planRepository.findAllByUser(user);
        List<PlanResponse> responses = new ArrayList<>();
        for (PlanEntity plan : plans) {
            responses.add(new PlanResponse(plan.getId(), plan.getPlan_name(), plan.getCategory().getName(),
                    plan.getStartDate(), plan.getEndDate(), plan.getMinutes(), plan.isStatus()));
        }
        return responses;
    }

    public PlanResponse getPlan(Long planId, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(planId, user);
        return new PlanResponse(plan.getId(), plan.getPlan_name(), plan.getCategory().getName(),
                plan.getStartDate(), plan.getEndDate(), plan.getMinutes(), plan.isStatus());
    }
    public void addPlan(PlanRequest request, UserEntity user) {
        CategoryEntity category= getCategory(request.getCategory(), user);
        PlanEntity plan= request.toEntity(user, category);
        planRepository.save(plan);
        log.info("계획 저장 완료");
    }

    public void updatePlan(Long id, PlanRequest request, UserEntity user) {
        //유저, 계획 검사
        PlanEntity plan= getPlanByUserAndId(id, user);
        CategoryEntity category= getCategory(request.getCategory(), user);
        //reqeust에 들어온 값 확인, 값이 있고 빈 문자열이 아닐 경우에만 처리 (시간은
        plan.updatePlanName(request.getPlanName());
        plan.updateCategory(category);
        plan.updateDate(request.getStartDate(), request.getEndDate());
        plan.updateMinutes(request.getPlanMinutes());
        //여기서 값 바뀐 거만 수정해 줌..
        /*나중에 추가할 로직
           일자, 공부시간이 달라졌다면 타이머랑 비교해서 미완료로 처리
           타이머는 일자 수정 X
         */
    }

    //상태 변경 로직
    public void updateStatus(Long id, Boolean status, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(id, user);
        plan.updateStatus(status); //상태 변경
    }

    //삭제 로직
    public void deletePlan(Long id, UserEntity user) {
        PlanEntity plan= planRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
        planRepository.delete(plan);
    }

    //유저, planId 검사
    public PlanEntity getPlanByUserAndId(Long id, UserEntity user) {
        return planRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
    }
    //유효성 검사 후 카테고리 가져옴
    public CategoryEntity getCategory(Long category, UserEntity user) {
        //유저에게 존재하지 않는 카테고리일 경우
        if (!categoryRepository.existsByUserAndId(user, category)){
            log.info("계획 등록 실패");
            throw new NotFoundException("존재하지 않는 카테고리입니다.");
        }
        //반환되는 카테고리도 영속 상태임 (@Transactional 써서 메서드 끝날 때까지 영속)
        return categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다"));
    }
    /*
    public void checkStatus(Long planId, UserEntity user) {
        //일
    }*/
}
