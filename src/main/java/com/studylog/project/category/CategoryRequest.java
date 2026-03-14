package com.studylog.project.category;

import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "카테고리 등록/수정 request")
public record CategoryRequest(
        @Schema(description = "카테고리명 (특수 문자 입력 X, 10자 이내)")
        @NotBlank(message = "카테고리명을 입력해 주세요.")
        @Size(max= 10, message ="카테고리명은 10자 이내여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "특수 문자, 자음, 모음은 사용할 수 없습니다.")
        String name,

        @NotNull(message = "색상을 선택해 주세요.")
        String bgColor,
        String textColor
) {
    public CategoryRequest {
        // 초기화할 때 trim 적용됨
        name = name.trim();
        bgColor = bgColor.trim().toUpperCase();
        textColor = textColor.trim().toUpperCase();
    }

    public CategoryEntity toEntity (UserEntity user) {
        return CategoryEntity.builder()
                .name(name)
                .user_id(user)
                .bgColor(bgColor)
                .textColor(textColor)
                .build();
    }
}
