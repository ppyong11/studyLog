package com.studylog.project.timer;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.studylog.project.category.QCategoryEntity.categoryEntity;
import static com.studylog.project.plan.QPlanEntity.planEntity;
import static com.studylog.project.timer.QTimerEntity.timerEntity;

@AllArgsConstructor
public class TimerRepositoryImpl implements TimerRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<TimerResponse> searchTimersByFilter(UserEntity user, LocalDate startDate, LocalDate endDate,
                                                     List<Long> categoryList, String planKeyword, String keyword, String status, String sort,
                                                     int page) {
        long pageSize = 20;
        long offset= (page - 1) * pageSize;

        return queryFactory
                .select(Projections.constructor(
                        TimerResponse.class,
                        timerEntity.id,
                        timerEntity.name,
                        categoryEntity.id, //Projections이라 join 영속성 관리 안 됨 -> 바로 DTO 매핑이기 때문
                        timerEntity.createAt,
                        timerEntity.startAt,
                        timerEntity.endAt,
                        timerEntity.pauseAt,
                        timerEntity.elapsed,
                        timerEntity.status,
                        planEntity.id,
                        planEntity.name,
                        planEntity.category.id,
                        planEntity.minutes,
                        planEntity.startDate,
                        planEntity.endDate,
                        planEntity.isComplete
                ))
                .from(timerEntity)
                .leftJoin(timerEntity.plan, planEntity) //select(엔티티) 아니라서 fetchJoin 쓰면 오류남
                .leftJoin(timerEntity.category, categoryEntity) // 뒤에 건 별칭
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

    private OrderSpecifier<?> getOrderSpecifiers(String sort) {
        String[] arr= sort.split(",");

        if(arr.length != 2) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
        }

        String field= arr[0].trim().toLowerCase();
        String value= arr[1].trim().toLowerCase();

        boolean isDesc = value.equals("desc");

        if(!value.equals("asc") && !value.equals("desc")) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
        }

        return isDesc ? timerEntity.createAt.desc() : timerEntity.createAt.asc();
    }
}
