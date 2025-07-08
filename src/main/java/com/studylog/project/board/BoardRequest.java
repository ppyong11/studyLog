package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
public class BoardRequest {
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max= 30, message = "제목은 30자 이내여야 합니다.")
    private String title;
    private String content;

    public BoardEntity toEntity(UserEntity user, CategoryEntity category) {
        return BoardEntity.builder()
                .user_id(user)
                .category_id(category)
                .title(title)
                .content(content)
                //upload, update는 엔티티에서 초기화
                .build();
    }
}
