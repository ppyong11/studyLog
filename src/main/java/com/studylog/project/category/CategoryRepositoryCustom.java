package com.studylog.project.category;

import com.studylog.project.user.UserEntity;

import java.util.List;

public interface CategoryRepositoryCustom {

    List<CategoryResponse> findAllCategories(UserEntity user);
    List<CategoryResponse> searchCategoriesByFilter(UserEntity user, String keyword, int page);
    Long totalItems(UserEntity user, String keyword);
}
