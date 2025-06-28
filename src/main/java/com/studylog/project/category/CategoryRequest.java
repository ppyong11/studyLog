package com.studylog.project.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CategoryRequest {
    @NotBlank(message = "카테고리명을 입력해 주세요.")
    @Size(max= 10, message ="카테고리명은 10자 이내여야 합니다.")
    private String name;
}
