package com.studylog.project.category;

import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public void defaultCategory(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user) //알아서 long타입으로 들어감
                .category_name("기타")
                .build();
    }
}

