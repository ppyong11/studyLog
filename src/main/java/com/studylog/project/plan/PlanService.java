package com.studylog.project.plan;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.hibernate.query.spi.QueryPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final JPAQueryFactory queryFactory; //동적 쿼리용


    public PlanResponse getPlan(Long planId, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(planId, user);
        return new PlanResponse(plan.getId(), plan.getPlan_name(), plan.getCategory().getName(),
                plan.getStartDate(), plan.getEndDate(), plan.getMinutes(), plan.isStatus());
    }

    public List<PlanResponse> searchPlans(UserEntity user, LocalDate startDate, LocalDate endDate,
                                          List<Long> categoryList, String keyword, Boolean status, List<String> sort) {
        QPlanEntity planEntity = QPlanEntity.planEntity;

        //where 조립 빌더
        BooleanBuilder builder = new BooleanBuilder();

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> dateOrder = null;

        for(String s : sort){
            String[] arr= s.split(",");
            if(arr.length != 2){
                throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
            String field= arr[0].trim().toLowerCase();
            String value= arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc")){
                throw new BadRequestException("지원하지 않는 정렬입니다.");
            }

            switch (field){
                case "date" ->
                    dateOrder= value.equals("desc")? planEntity.startDate.desc() : planEntity.startDate.asc();
                case "category" ->
                    orders.add(value.equals("desc")? planEntity.category.name.desc() : planEntity.category.name.asc());
                default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
        }

        //date 정렬 맨 앞으로 (우선)
        if(dateOrder != null){
            orders.add(0, dateOrder); //카테고리가 index 0에 있다면 뒤로 가짐
        }

        builder.and(planEntity.user.eq(user)); //유저 것만 조회 결과로
        if(startDate != null) {
            if (endDate != null) {
                //start~end
                builder.and(planEntity.endDate.loe(endDate));
            }
            //start~전 일자
            builder.and(planEntity.startDate.goe(startDate));
        }
        if(!categoryList.isEmpty()) {
            //빈 리스트가 아니라면, 빈 리스트인데 실행 시 모든 조건이 false처리됨 (and니께)
            builder.and(planEntity.category.id.in(categoryList)); //in(1, 2, 3) 일케 들어감
        }
        if(keyword != null && !keyword.isEmpty()) {
            builder.and(planEntity.plan_name.like('%' + keyword + '%'));
        }
        if(status != null) {
            builder.and(planEntity.status.eq(status));
        }

        List<PlanEntity> plans= queryFactory.selectFrom(planEntity) //select * from planEntity
                .where(builder)
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .fetch(); //전체 결과 반환 (List<planEntity> 타입), 결과 없을 시 빈 리스트 (Null 반환 X)

        return plans.stream()
                .map(plan -> PlanResponse.toDto(plan))
                .toList();
    }

    public void addPlan(PlanRequest request, UserEntity user) {
        CategoryEntity category= getCategory(request.getCategoryId(), user);
        PlanEntity plan= request.toEntity(user, category);
        planRepository.save(plan);
        log.info("계획 저장 완료");
    }

    public void updatePlan(Long id, PlanRequest request, UserEntity user) {
        //유저, 계획 검사
        PlanEntity plan= getPlanByUserAndId(id, user);
        CategoryEntity category= getCategory(request.getCategoryId(), user);
        //reqeust에 들어온 값 확인, 값이 있고 빈 문자열이 아닐 경우에만 처리 (시간은
        plan.updatePlan(request, category);
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
        planRepository.delete(plan); //DB cascade로 타이머도 삭제됨
    }

    //유저, planId 검사
    private PlanEntity getPlanByUserAndId(Long id, UserEntity user) {
        return planRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
    }
    //유효성 검사 후 카테고리 가져옴
    private CategoryEntity getCategory(Long category, UserEntity user) {
        //유저에게 존재하지 않는 카테고리일 경우 반환 X
        //반환되는 카테고리도 영속 상태임 (@Transactional 써서 메서드 끝날 때까지 영속)
        return categoryRepository.findByUserAndId(user, category)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다"));
    }
}
