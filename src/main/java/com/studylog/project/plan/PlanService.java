package com.studylog.project.plan;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.ScrollResponse;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.timer.QTimerEntity;
import com.studylog.project.timer.TimerService;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final JPAQueryFactory queryFactory; //동적 쿼리용
    private final TimerService timerService;

    public PlanResponse getPlan(Long planId, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(planId, user);
        return PlanResponse.toDto(plan);
    }

    public ScrollPlanResponse searchTablePlans(UserEntity user, LocalDate startDate, LocalDate endDate,
                                               List<Long> categoryList, String keyword, Boolean status, List<String> sort,
                                               int page) {
        QPlanEntity planEntity = QPlanEntity.planEntity;
        //where 조립 빌더
        BooleanBuilder builder = new BooleanBuilder();

        OrderSpecifier<?>[] orders= new OrderSpecifier[2];

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
                    orders[0]= value.equals("desc")? planEntity.startDate.desc() : planEntity.startDate.asc();
                case "category" ->
                    orders[1]= value.equals("desc")? planEntity.category.name.desc() : planEntity.category.name.asc();
                default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
        }
        if(orders[0] == null || orders[1] == null) throw new BadRequestException("지원하지 않는 정렬입니다.");

        builder.and(planEntity.user.eq(user)); //유저 것만 조회 결과로
        //startDate, endDate 항상 있음 (컨트롤러에서 검증)
        builder.and(planEntity.startDate.goe(startDate)); // >=
        builder.and(planEntity.endDate.loe(endDate)); // <=

        if(!categoryList.isEmpty()) {
            //빈 리스트가 아니라면, 빈 리스트인데 실행 시 모든 조건이 false처리됨 (and니께)
            builder.and(planEntity.category.id.in(categoryList)); //in(1, 2, 3) 일케 들어감
        }
        if(keyword != null && !keyword.isEmpty()) {
            builder.and(planEntity.name.like('%' + keyword + '%'));
        }
        if(status != null) {
            builder.and(planEntity.isComplete.eq(status));
        }

        return getScrollPlanResponse(planEntity, builder, orders, page, user, null); //range: 일, 주, 월
    }

    public ScrollPlanResponse MainDailyPlans(UserEntity user, LocalDate today, int page){
        QPlanEntity planEntity= QPlanEntity.planEntity;

        OrderSpecifier<?>[] orders= {
                planEntity.startDate.asc(),
                planEntity.category.name.asc()
        };

        BooleanBuilder builder= new BooleanBuilder();

        builder.and(planEntity.startDate.loe(today)); //<=
        builder.and(planEntity.endDate.goe(today)); // >=

        return getScrollPlanResponse(planEntity, builder, orders, page, user, "일");
    }

    public List<CalenderPlanResponse> getCalenderPlans(LocalDate startDate, LocalDate endDate, String range, UserEntity user){
        QPlanEntity planEntity= QPlanEntity.planEntity;
        BooleanBuilder builder= new BooleanBuilder();
        LocalDate today= LocalDate.now(); //요청 시점 날짜 받기
        boolean isSame= false; //지역변수는 초기화 필요

        LocalDate mon, sun;

        switch (range){
            case "weekly" -> {
                mon= today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); //오늘 날짜 기준 주의 월요일 받기
                sun= today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                if(startDate.equals(mon) && endDate.equals(sun)) isSame= true;
            }
            case "monthly" -> {
                if(startDate.equals(startDate.with(TemporalAdjusters.firstDayOfMonth())) &&
                endDate.equals(endDate.with(TemporalAdjusters.lastDayOfMonth())))
                    isSame= true;
            }
            default -> throw new BadRequestException("잘못된 범위 값입니다.");
        }

        if(!isSame) throw new BadRequestException("잘못된 범위 값입니다.");
        builder.and(planEntity.startDate.loe(today)); //<=
        builder.and(planEntity.endDate.goe(today)); // >=

        return queryFactory
                .select(Projections.constructor(
                CalenderPlanResponse.class,
                        planEntity.id,
                        planEntity.name,
                        planEntity.category.id,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.isComplete
        ))
                .from(planEntity)
                .where(builder)
                .orderBy(planEntity.startDate.asc(),
                        planEntity.category.name.asc()) //user_id + category_name으로 인덱싱됨 (join 후 정렬)
                .fetch();
    }

    private ScrollPlanResponse getScrollPlanResponse (QPlanEntity planEntity, BooleanBuilder builder, OrderSpecifier<?>[] orders,
                                                    int page, UserEntity user, String range){

        long pageSize= 10;
        long offset= (page-1) * pageSize; //0~9, 10~19

        List<PlanResponse> planResponse = queryFactory
                .select(Projections.constructor(
                        PlanResponse.class,
                        planEntity.id,
                        planEntity.name,
                        planEntity.memo,
                        planEntity.category.id,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.minutes,
                        planEntity.isComplete
                ))
                .from(planEntity)
                .where(builder)
                .orderBy(orders)
                .offset(offset)
                .limit(pageSize)
                .fetch(); //전체 결과 반환 (List<planResponse> 타입), 결과 없을 시 빈 리스트 (Null 반환 X)

        long[] planCount= getPlanCounts(builder);
        long total= planCount[0];
        long achieved= planCount[1];

        //일, 주, 월 범위일 때만 메시지 함께 반환
        double rate = total == 0 ? 0.0 : (double) achieved / total * 100;
        String totalStudyTime= totalStudyTime(builder);
        boolean hasNext= page * pageSize < total;

        String message= returnMessage(user.getNickname(), rate, total, range);
        return ScrollPlanResponse.toDto(planResponse, achieved, total, rate, message, totalStudyTime, page, hasNext);
    }

    private long[] getPlanCounts(BooleanBuilder builder){
        QPlanEntity planEntity= QPlanEntity.planEntity;

        NumberExpression<Long> totalExpr= planEntity.count();
        NumberExpression<Long> achievedExpr= new CaseBuilder()
                .when(planEntity.isComplete.isTrue())
                .then(1L)
                .otherwise(0L)
                .sum(); // achievedCount

        Tuple planCount= queryFactory
                            .select(totalExpr, achievedExpr)
                            .from(planEntity)
                            .where(builder) //조회 결과 중 달성한 계획 count
                            .fetchOne(); //계획 개수 받음

        Long totalLong= planCount != null? planCount.get(totalExpr) : null;
        long totalCount= totalLong != null? totalLong : 0L;

        Long achievedLong= planCount != null? planCount.get(achievedExpr) : null;
        long achievedCount= achievedLong != null? achievedLong : 0L;

        return new long[]{totalCount, achievedCount};
    }

    private String totalStudyTime(BooleanBuilder builder){
        QPlanEntity plan= QPlanEntity.planEntity;
        QTimerEntity timer= QTimerEntity.timerEntity; //결합도 문제 X(성능 최적화 차원)

        NumberExpression<Long> totalSencodsSelect= new CaseBuilder()
                .when(timer.elapsed.isNotNull()) //계획이 설정된 타이머가 있으면
                .then(timer.elapsed)
                .when(timer.isNull().and(plan.isComplete.isTrue())) //타이머가 없고 계획이 완료됐다면
                .then(Expressions.numberTemplate(Long.class, "{0} * 60", plan.minutes)) //int * 60을 Long으로 변환
                .otherwise(0L)
                .sum(); //전체합계

        Long totalSecondsDecimal= queryFactory
                .select(totalSencodsSelect)
                .from(plan)
                .leftJoin(timer).on(timer.plan.id.eq(plan.id)) //Plan에 Timer 필드가 없어서 직접 조인 (fetchJoin 안됨 + 단순 조회)
                .where(builder)
                .fetchOne();

        long totalSeconds = totalSecondsDecimal == null ? 0L : totalSecondsDecimal;
        long hours= totalSeconds / 3600;
        long minutes= (totalSeconds % 3600) /60;
        long seconds= totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String returnMessage(String nickname, double rate, long total, String range){
        if(range == null) return null;

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
            if(range.equals("day")) return "계획의 반을 완료했어요! 잘하고 있어요 👏";
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

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        planRepository.updateCategory(deleteCategory, defaultCategory);
    }

    public void addPlan(PlanRequest request, UserEntity user) {
        CategoryEntity category= getCategory(request.getCategoryId(), user);
        PlanEntity plan= request.toEntity(user, category);
        planRepository.save(plan);
    }

    public void updatePlan(Long id, PlanRequest request, UserEntity user) {
        //유저, 계획 검사
        PlanEntity plan= getPlanByUserAndId(id, user);
        CategoryEntity category= getCategory(request.getCategoryId(), user);
        //reqeust에 들어온 값 확인, 값이 있고 빈 문자열이 아닐 경우에만 처리 (시간은

        //타이머 있으면 처리, 없으면 패스
        timerService.getTimerByPlan(plan)
                        .ifPresent(timer -> {
                            timer.updateCategory(category);
                        });

        plan.updatePlan(request, category);
        //여기서 값 바뀐 거만 수정해 줌..
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

        planRepository.delete(plan); //cascade로 타이머도 삭제됨
    }

    //타이머에 보내는 계획 리스트
    public ScrollResponse<PlansForTimerResponse> getPlansForTimer(LocalDate startDate, LocalDate endDate, String keyword,
                                                                  String sort, int page, UserEntity user){
        QPlanEntity planEntity= QPlanEntity.planEntity;
        QTimerEntity timerEntity= QTimerEntity.timerEntity;

        BooleanBuilder builder= new BooleanBuilder();
        OrderSpecifier<?> order;

        switch (sort){
            case ("asc")-> order= planEntity.startDate.asc();
            case ("desc")-> order= planEntity.startDate.desc();
            default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
        }

        builder.and(planEntity.user.eq(user));
        //startDate, endDate 항상 있음
        builder.and(planEntity.startDate.goe(startDate)); // >=
        builder.and(planEntity.endDate.loe(endDate)); // <=
        builder.and(planEntity.id.notIn( //서브쿼리 결과 포함 X
                JPAExpressions
                        .select(timerEntity.plan.id)
                        .from(timerEntity)
                        .where(timerEntity.plan.isNotNull()))); //설정한 계획 안 뜸 (서브쿼리)
        /*
        where plan_id not in (
            select t.plan_id
            from timer t
            where t.plan_id is not null
        )
        */
        builder.and(planEntity.isComplete.isFalse()); //완료된 계획 안 뜸

        if(keyword != null && !keyword.isEmpty())
            builder.and(planEntity.name.like('%' + keyword + '%'));

        long pageSize= 20;
        long offset= (page - 1) * pageSize;
        List<PlansForTimerResponse> responses= queryFactory
                                    .select(Projections.constructor(
                                            PlansForTimerResponse.class,
                                            planEntity.id,
                                            planEntity.name,
                                            planEntity.startDate,
                                            planEntity.endDate
                                    ))
                .from(planEntity)
                .where(builder)
                .orderBy(order)
                .offset(offset)
                .limit(pageSize)
                .fetch();

        Long totalItems= queryFactory
                .select(planEntity.count())
                .from(planEntity)
                .where(builder)
                .fetchOne();

        boolean hasNext= page * pageSize < totalItems;
        return new ScrollResponse<>(responses, totalItems, page, pageSize, hasNext);
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
