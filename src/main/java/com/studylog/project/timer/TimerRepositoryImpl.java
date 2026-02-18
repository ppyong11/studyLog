package com.studylog.project.timer;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.studylog.project.timer.QTimerEntity.timerEntity;

@AllArgsConstructor
public class TimerRepositoryImpl implements TimerRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<TimerResponse> searchTimersByFilter(UserEntity user, LocalDate startDate, LocalDate endDate,
                                                     List<Long> categoryList, String planKeyword, String keyword, String status, List<String> sort,
                                                     int page) {
        long pageSize = 20;
        long offset= (page - 1) * pageSize;

        return queryFactory
                .select(Projections.constructor(
                        TimerResponse.class,
                        timerEntity.id,
                        timerEntity.name,
                        timerEntity.plan.id, //외래키로 연결된 다른 테이블 컬럼값 가져오려면 join 필수
                        timerEntity.plan.name,
                        timerEntity.category.id, //Projections이라 join 영속성 관리 안 됨 -> 바로 DTO 매핑이기 때문
                        timerEntity.createAt,
                        timerEntity.startAt,
                        timerEntity.endAt,
                        timerEntity.pauseAt,
                        timerEntity.elapsed,
                        timerEntity.status
                ))
                .from(timerEntity)
                .leftJoin(timerEntity.plan) //select(엔티티) 아니라서 fetchJoin 쓰면 오류남
                .leftJoin(timerEntity.category)
                .where(
                        timerEntity.user.eq(user),
                        dateRange(startDate, endDate),
                        planNameLike(planKeyword),
                        timerNameLike(keyword),
                        categoryIn(categoryList),
                        statusEqual(status)
                )
                .orderBy(getOrderSpecifiers(sort))
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    public Long getTotalItems(UserEntity user, LocalDate startDate, LocalDate endDate,
                              List<Long> categoryList, String planKeyword, String keyword, String status) {
        Long totalItems= queryFactory
                .select(timerEntity.count())
                .from(timerEntity)
                .where(
                        timerEntity.user.eq(user),
                        dateRange(startDate, endDate),
                        planNameLike(planKeyword),
                        timerNameLike(keyword),
                        categoryIn(categoryList),
                        statusEqual(status)
                )
                .fetchOne();

        return totalItems != null? totalItems : 0L;
    }

    private BooleanExpression dateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return null;

        return timerEntity.createAt.goe(startDate.atTime(LocalTime.MIN))
                .and(timerEntity.createAt.loe(endDate.atTime(LocalTime.MAX)));
    }

    private BooleanExpression planNameLike(String planKeyword) {
        return (planKeyword != null && !planKeyword.isEmpty()) ?
            timerEntity.plan.name.like('%' + planKeyword + '%') : null;
    }

    private BooleanExpression timerNameLike(String keyword) {
        return (keyword != null && !keyword.isEmpty()) ?
                timerEntity.name.like('%' + keyword + '%') : null;
    }
    private BooleanExpression categoryIn(List<Long> categoryIds) {
        return (!categoryIds.isEmpty()) ? timerEntity.category.id.in(categoryIds) : null;
    }

    private BooleanExpression statusEqual(String status) {
        return (status != null) ?
            timerEntity.status.eq(TimerStatus.valueOf(status)) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(List<String> sort) {
        OrderSpecifier<?>[] orders= new OrderSpecifier[3];

        for(String s:sort){
            String[] arr= s.split(",");
            if(arr.length != 2) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }
            String field= arr[0].trim().toLowerCase();
            String value= arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc")) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }

            switch (field){
                case "date" ->
                        orders[0]= value.equals("desc")? timerEntity.createAt.desc(): timerEntity.createAt.asc();
                case "category" ->
                        orders[1]= value.equals("desc")? timerEntity.category.name.desc():timerEntity.category.name.asc();
                case "name" ->
                        orders[2]= value.equals("desc")? timerEntity.name.desc(): timerEntity.name.asc();
                default -> CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }
        }

        if(orders[0] == null || orders[1] == null || orders[2] == null) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
        }

        return orders;
    }
}
