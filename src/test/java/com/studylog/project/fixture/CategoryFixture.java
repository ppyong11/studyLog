package com.studylog.project.fixture;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.user.UserEntity;

public class CategoryFixture {
    public static CategoryEntity createAndSaveCategory(UserEntity user, CategoryRepository repo) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user)
                .name("테스트")
                .bgColor("#F7F7F7")
                .textColor("#484848")
                .build();

        return repo.save(category);
    }
}
