package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 생성 request")
public record BoardRequest (
    @Schema(description = "카테고리 id *필수")
    @NotNull(message = "카테고리를 선택해 주세요.")
    Long categoryId,
    @Schema(description = "게시글 제목 (30자 이내)")
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max= 30, message = "제목은 30자 이내여야 합니다.")
    String title,
    @Schema(description = "게시글 내용")
    String content
) {
    public BoardRequest {
        title = title().trim();
    }

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
