package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;

    public void addPlan(PlanCreateRequest request, UserEntity user) {
        //유저에게 존재하지 않는 카테고리일 경우
        if (!categoryRepository.existsByUserAndId(user, request.getCategory())){
            log.info("계획 등록 실패");
            throw new NotFoundException("존재하지 않는 카테고리입니다.");
        }
        CategoryEntity category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다"));
        if(request.getStartDate().isAfter(request.getEndDate())){
            throw new BadRequestException("시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
        }
        if(request.getPlanMinutes() < 0){
            throw new BadRequestException("음수값은 입력될 수 없습니다.");
        }
        PlanEntity plan= request.toEntity(user, category);
        planRepository.save(plan);
        log.info("계획 저장 완료");
    }

    public void updatePlan(PlanCreateRequest request, UserEntity user) {

    }
}
