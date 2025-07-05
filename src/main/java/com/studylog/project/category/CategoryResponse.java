package com.studylog.project.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CategoryResponse {
    private Long id;
    private String name;

    public static CategoryResponse toDto(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}