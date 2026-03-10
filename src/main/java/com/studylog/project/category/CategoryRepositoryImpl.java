package com.studylog.project.category;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;

import java.util.List;

import static com.studylog.project.category.QCategoryEntity.categoryEntity;

@AllArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CategoryResponse> findAllCategories(UserEntity user) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CategoryResponse.class,
                                categoryEntity.id,
                                categoryEntity.name,
                                categoryEntity.bgColor,
                                categoryEntity.textColor
                        ))
                .from(categoryEntity)
                .where(categoryEntity.user.eq(user))
                .fetch();
    }

    @Override
    public List<CategoryResponse> searchCategoriesByFilter(UserEntity user, String keyword, int page) {

        long pageSize = 10;
        long offset= (page-1) * pageSize; //페이지당 10건씩 반환 (0~9, 10~19)

        return queryFactory
                .select(Projections.constructor(
                        CategoryResponse.class,
                        categoryEntity.id,
                        categoryEntity.name,
                        categoryEntity.bgColor,
                        categoryEntity.textColor
                ))
                .from(categoryEntity)
                .where(
                        categoryEntity.user.eq(user),
                        nameLike(keyword)
                )
                .orderBy(categoryEntity.name.asc())
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long totalItems(UserEntity user, String keyword) {
        return queryFactory
                .select(categoryEntity.count())
                .from(categoryEntity)
                .where(
                        categoryEntity.user.eq(user),
                        nameLike(keyword)
                )
                .fetchOne();
    }

    private BooleanExpression nameLike(String keyword) {
        return (keyword != null && !keyword.isEmpty()) ? categoryEntity.name.like('%' + keyword + '%') : null;
    }

}
