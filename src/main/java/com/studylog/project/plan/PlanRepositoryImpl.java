package com.studylog.project.plan;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static com.studylog.project.plan.QPlanEntity.planEntity;
import static com.studylog.project.timer.QTimerEntity.timerEntity;

@AllArgsConstructor
public class PlanRepositoryImpl implements PlanRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public PlanSummary getPlanSummaryByFilter(UserEntity user, LocalDate startDate, LocalDate endDate, List<Long> categoryIds,
                                             String keyword, Boolean status, List<String> sort, int page) {
        long pageSize= 10;
        long offset= (page-1) * pageSize; //0~9, 10~19

        BooleanBuilder builder = new BooleanBuilder(
                planEntity.user.eq(user) //유저 것만 조회 결과로
                //startDate, endDate 항상 있음 (컨트롤러에서 검증)
                .and(planEntity.startDate.goe(startDate)) // >=
                .and(planEntity.endDate.loe(endDate)) // <=
                .and(categoryId(categoryIds))
                .and(nameLike(keyword))
                .and(statusEqual(status))
        );

        List<PlanResponse> responses =  queryFactory
                .select(Projections.constructor(
                        PlanResponse.class,
                        planEntity.id,
                        planEntity.name,
                        planEntity.memo,
                        planEntity.category.id,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.minutes,
                        planEntity.isComplete,
                        timerEntity.id, // 연결된 타이머 없으면 null 반환 (left join)
                        timerEntity.name,
                        timerEntity.category.id,
                        timerEntity.createAt,
                        timerEntity.startAt,
                        timerEntity.elapsed
                ))
                .from(planEntity)
                .leftJoin(planEntity.timer, timerEntity)
                .where(
                        planEntity.user.eq(user), //유저 것만 조회 결과로
                        //startDate, endDate 항상 있음 (컨트롤러에서 검증)
                        planEntity.startDate.goe(startDate), // >=
                        planEntity.endDate.loe(endDate), // <=
                        categoryId(categoryIds),
                        nameLike(keyword),
                        statusEqual(status)
                )
                .orderBy(getOrderSpecifiers(sort))
                .offset(offset)
                .limit(pageSize)
                .fetch(); //전체 결과 반환 (List<planResponse> 타입), 결과 없을 시 빈 리스트 (Null 반환 X)

        List<Long> getCount = getPlanTotalCountAndAchivedCount(builder);

        Long totalSeconds = getTotalSeconds(builder);
        totalSeconds = totalSeconds != null ? totalSeconds : 0L;

        return new PlanSummary(responses, getCount.get(0), getCount.get(1), totalSeconds);
    }

    @Override
    public List<PlanResponse> getCalendarPlans(LocalDate startDate, LocalDate endDate, UserEntity user) {
        return queryFactory
                .select(Projections.constructor(
                        PlanResponse.class,
                        planEntity.id,
                        planEntity.name,
                        planEntity.memo,
                        planEntity.category.id,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.minutes,
                        planEntity.isComplete,
                        timerEntity.id,
                        timerEntity.name,
                        timerEntity.category.id,
                        timerEntity.createAt,
                        timerEntity.startAt,
                        timerEntity.elapsed
                ))
                .from(planEntity)
                .leftJoin(planEntity.timer, timerEntity)
                .where(
                        planEntity.user.eq(user),
                        planEntity.startDate.loe(endDate), //<=
                        planEntity.endDate.goe(startDate) // >=
                )
                .orderBy(planEntity.startDate.asc(),
                        planEntity.category.name.asc()) //user_id + category_name으로 인덱싱됨 (join 후 정렬)
                .fetch();
    }

    @Override
    public PlanSummary findTodayPlans(UserEntity user, LocalDate today, int page) {
        long pageSize= 10;
        long offset= (page-1) * pageSize; //0~9, 10~19

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(planEntity.user.eq(user)); //유저 것만 조회 결과로
        //startDate, endDate 항상 있음 (컨트롤러에서 검증)
        builder.and(planEntity.startDate.goe(today)); // >=
        builder.and(planEntity.endDate.loe(today)); // <=

        List<PlanResponse> responses = queryFactory
                .select(Projections.constructor(
                        PlanResponse.class,
                        planEntity.id,
                        planEntity.name,
                        planEntity.memo,
                        planEntity.category.id,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.minutes,
                        planEntity.isComplete,
                        timerEntity.id,
                        timerEntity.name,
                        timerEntity.category.id,
                        timerEntity.createAt,
                        timerEntity.startAt,
                        timerEntity.elapsed
                ))
                .from(planEntity)
                .leftJoin(planEntity.timer, timerEntity)
                .where(
                        planEntity.user.eq(user),
                        planEntity.startDate.loe(today), //<=
                        planEntity.endDate.goe(today) // >=
                )
                .orderBy(
                        planEntity.startDate.asc(),
                        planEntity.category.name.asc()
                )
                .offset(offset)
                .limit(pageSize)
                .fetch(); //전체 결과 반환 (List<planResponse> 타입), 결과 없을 시 빈 리스트 (Null 반환 X)

        List<Long> getCount = getPlanTotalCountAndAchivedCount(builder);

        Long totalSeconds = getTotalSeconds(builder);
        totalSeconds = totalSeconds != null ? totalSeconds : 0L;

        return new PlanSummary(responses, getCount.get(0), getCount.get(1), totalSeconds);
    }

    public PlansForTimerSummary getPlansForTimer(LocalDate startDate, LocalDate endDate, String keyword,
                                                  String sort, int page, UserEntity user) {
        long pageSize = 20;
        long offset = (page - 1) * pageSize;

        OrderSpecifier<?> order = planEntity.startDate.asc();
        BooleanBuilder builder= new BooleanBuilder();

        if ("desc".equals(sort)) {
            order= planEntity.startDate.desc();
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
        builder.and(nameLike(keyword));

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

        totalItems = totalItems != null? totalItems : 0L;

        return new PlansForTimerSummary(responses, totalItems);
    }

    private List<Long> getPlanTotalCountAndAchivedCount(BooleanBuilder builder) {
        NumberExpression<Long> totalExpr= planEntity.count();
        NumberExpression<Long> achievedExpr= new CaseBuilder()
                .when(planEntity.isComplete.isTrue())
                .then(1L)
                .otherwise(0L)
                .sum(); // achievedCount


        Tuple planCount = queryFactory
                .select(totalExpr, achievedExpr)
                .from(planEntity)
                .where(builder) //조회 결과 중 달성한 계획 count
                .fetchOne(); //계획 개수 받음

        Long totalCount = planCount != null? planCount.get(totalExpr) : 0L;
        Long achievedCount= planCount != null? planCount.get(achievedExpr) : 0L;

        return List.of(totalCount == null? 0L : totalCount, achievedCount == null? 0L : achievedCount); // List.of()은 null 허용 X, null 방지 처리 필수
    }

    private Long getTotalSeconds(BooleanBuilder builder) {
        NumberExpression<Long> totalSencodsSelect= new CaseBuilder()
                .when(timerEntity.elapsed.isNotNull()) //계획이 설정된 타이머가 있으면
                .then(timerEntity.elapsed)
                .when(timerEntity.isNull().and(planEntity.isComplete.isTrue())) //타이머가 없고 계획이 완료됐다면
                .then(Expressions.numberTemplate(Long.class, "{0} * 60", planEntity.minutes)) //int * 60을 Long으로 변환
                .otherwise(0L)
                .sum(); //전체합계

        return queryFactory
                .select(totalSencodsSelect)
                .from(planEntity)
                .leftJoin(timerEntity).on(timerEntity.plan.id.eq(planEntity.id)) //Plan에 Timer 필드가 없어서 직접 조인 (fetchJoin 안됨 + 단순 조회)
                .where(builder)
                .fetchOne();
    }

    private BooleanExpression categoryId(List<Long> categoryIds) {
        return (categoryIds != null && !categoryIds.isEmpty()) ? planEntity.category.id.in(categoryIds) : null;
    }

    private BooleanExpression nameLike(String keyword) {
        return (keyword != null && !keyword.isEmpty()) ?
                planEntity.name.like("%" + keyword + "%") : null;
    }

    private BooleanExpression statusEqual(Boolean status) {
        return (status != null) ? planEntity.isComplete.eq(status) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(List<String> sort) {
        OrderSpecifier<?>[] orders= new OrderSpecifier[2];

        for(String s : sort){
            String[] arr= s.split(",");
            if(arr.length != 2) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }
            String field= arr[0].trim().toLowerCase();
            String value= arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc")){
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }

            switch (field){
                case "date" ->
                        orders[0]= value.equals("desc")? planEntity.startDate.desc() : planEntity.startDate.asc();
                case "category" ->
                        orders[1]= value.equals("desc")? planEntity.category.name.desc() : planEntity.category.name.asc();
                default -> CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }
        }

        if(orders[0] == null || orders[1] == null) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
        }

        return orders;
    }
}
