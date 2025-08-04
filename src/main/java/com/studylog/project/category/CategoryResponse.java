package com.studylog.project.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CategoryResponse {
    @Schema(description = "카테고리 id", example = "1")
    private Long id;
    @Schema(description = "카테고리명", example = "공부")
    private String name;

    public static CategoryResponse toDto(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}