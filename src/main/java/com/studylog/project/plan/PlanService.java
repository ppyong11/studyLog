package com.studylog.project.plan;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerRepository;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final JPAQueryFactory queryFactory; //동적 쿼리용
    private final TimerRepository timerRepository;

    public PlanResponse getPlan(Long planId, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(planId, user);
        return new PlanResponse(plan.getId(), plan.getPlan_name(), plan.getCategory().getName(), plan.getPlan_memo(),
                plan.getStartDate(), plan.getEndDate(), plan.getMinutes(), plan.isStatus());
    }

    public PlanDetailResponse searchPlans(UserEntity user, LocalDate startDate, LocalDate endDate,
                               List<Long> categoryList, String keyword, Boolean status, List<String> sort, String range) {
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

        List<PlanResponse> planResponse= plans.stream()
                                            .map(plan -> PlanResponse.toDto(plan))
                                            .toList(); //통계 빼고 반환

        long totalCount= queryFactory.select(planEntity.count())
                .from(planEntity)
                .where(builder)
                .fetchOne();
        long achievedCount= queryFactory.select(planEntity.count())
                .from(planEntity)
                .where(builder.and(planEntity.status.isTrue())) //조회 결과 중 달성한 계획 count
                .fetchOne();

        //일, 주, 월 범위일 때만 메시지 함께 반환
        double rate = totalCount == 0 ? 0.0 : (double) achievedCount / totalCount * 100;

        if(range == null) return PlanDetailResponse.toDto(planResponse, achievedCount, totalCount, rate, null); //메시지는 null 처리

        String message= returnMessage(user.getNickname(), range, rate, totalCount);
        return PlanDetailResponse.toDto(planResponse, achievedCount, totalCount, rate, message);
    }

    private String returnMessage(String nickname, String range, double rate, long total){
        //range는 day, week, month만 받음 (컨트롤러에서 분기 처리)
        String unit= range.equals("week")? "주":"달";
        if(total == 0) return "해당 일자에 등록된 계획이 없어요.";

        if (rate == 0.0){
            if(range.equals("day")) return "아직 달성한 계획이 없어요. 시작해 볼까요? 😎";
            return String.format("이번 %s에 달성한 계획이 없어요. 지금부터 해도 충분해요 🍀",
                    unit);
        } else if (rate < 50.0) {
            if(range.equals("day")) return String.format("시작이 제일 어려운 거 아시죠? %s 님은 그걸 해냈어요!",
                    nickname);
            return "천천히 쌓아가는 중이에요. 남은 기간 동안 더 쌓아봐요! 🏃";
        } else if (rate < 70) {
            if(range.equals("day")) return "오늘 계획의 반을 완료했어요! 잘하고 있어요 👏";
            return String.format("한 %s 목표의 절반 이상을 완료했어요! 조금만 더 힘내 볼까요? 🔥",
                    unit);
        } else if (rate < 100) {
            if(range.equals("day")) return "거의 다 했네요! 마무리만 잘하면 완벽해요 🔥";
            return String.format("한 %s간 열심히 달렸네요! 이제 마무리만 남았어요 👊",
                    unit);

        } else{
            if(range.equals("day")) return "오늘 계획을 모두 완료했어요! 최고예요!";
            return String.format("🎉 이번 %s 목표 달성! %s 님의 꾸준한 노력의 결과예요. 멋져요!",
                    unit, nickname);
        }
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

        TimerEntity timer= timerRepository.findByPlan(plan);
        if(timer != null){
            timer.updateCategory(category);
        }

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
