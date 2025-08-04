package com.studylog.project.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = false) //DTO에 없는 필드 들어오면 에러
@Schema(description = "게시글 수정 request (변경 안 된 내용은 기존 값으로 채워짐)")
public class BoardUpdateRequest {
    @Schema(description = "카테고리 id *필수")
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;
    @Schema(description = "게시글 제목 (30자 이내)")
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max= 30, message = "제목은 30자 이내여야 합니다.")
    private String title;
    @Schema(description = "게시글 내용")
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
