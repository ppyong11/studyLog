package com.studylog.project.board;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.studylog.project.board.QBoardEntity.boardEntity; // 싱글톤 객체

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardResponse> searchBoardsByFilter(UserEntity user, List<Long> categoryIds, String keyword,
                                                List<String> sortList, int page) {

        long pageSize = 30;
        long offset= (page - 1) * pageSize; //페이지당 30건 반환

        return queryFactory
                .select(
                        Projections.constructor(
                                BoardResponse.class,
                                boardEntity.id,
                                boardEntity.category.id,
                                boardEntity.title,
                                boardEntity.content,
                                boardEntity.update_at,
                                boardEntity.update_at
                        ))
                .from(boardEntity)
                .where(
                    boardEntity.user.eq(user),
                        categoryIn(categoryIds),
                        titleLike(keyword)
                )
                .orderBy(getOrderSpecifiers(sortList))
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    // 총 개수 반환
    public Long getTotalItems(UserEntity user, List<Long> categoryIds, String keyword) {
        return queryFactory
                .select(boardEntity.count())
                .from(boardEntity)
                .where(
                        boardEntity.user.eq(user),
                        categoryIn(categoryIds),
                        titleLike(keyword)
                )
                .fetchOne(); // count()는 항상 row가 하나씩 있어서 0 이상 반환
    }

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        // null 반환 시 모든 카테고리 조회 (where절에서 조건이 사라짐)
        return (!categoryIds.isEmpty()) ? boardEntity.category.id.in(categoryIds) : null;
    }

    private BooleanExpression titleLike(String keyword) {
        return (keyword != null && !keyword.isEmpty()) ? boardEntity.title.like("%" + keyword + "%") : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(List<String> sortList) {
        OrderSpecifier<?>[] orders = new OrderSpecifier[3];

        for(String s : sortList) {
            String[] arr= s.split(","); //arr[0]= title, arr[1]= asc
            if(arr.length != 2) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sortList);
            }
            String field = arr[0].trim().toLowerCase();
            String value = arr[1].trim().toLowerCase();

            if(!value.equals("asc") && !value.equals("desc")) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sortList);
            }

            switch (field) { //->: 자동 break 처리
                case "date" ->
                        orders[0]= value.equals("desc")? boardEntity.upload_at.desc() : boardEntity.upload_at.asc();
                case "category" ->
                        orders[1]= value.equals("desc")? boardEntity.category.name.desc() : boardEntity.category.name.asc();
                case "title" ->
                        orders[2]= value.equals("desc")? boardEntity.title.desc() : boardEntity.title.asc();
                default -> CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sortList);

            }
        }

        if (orders[0] == null || orders[1] == null || orders[2] == null) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sortList);
        }

        return orders;
    }
}
