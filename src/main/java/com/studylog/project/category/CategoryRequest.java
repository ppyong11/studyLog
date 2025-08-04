package com.studylog.project.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(description = "카테고리 등록/수정 request")
public class CategoryRequest {
    @Schema(description = "카테고리명 (특수 문자 입력 X, 10자 이내)")
    @NotBlank(message = "카테고리명을 입력해 주세요.")
    @Size(max= 10, message ="카테고리명은 10자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣 ]+$", message = "특수 문자는 입력할 수 없습니다.")
    private String name;
}
