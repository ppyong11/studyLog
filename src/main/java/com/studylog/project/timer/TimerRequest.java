package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "타이머 등록/수정 request")
public record TimerRequest (
    @Schema(description = "타이머명 (20자 이내)")
    @NotBlank(message = "타이머 제목을 입력해 주세요.")
    @Size(max= 20, message = "20자 이내로 입력해 주세요.")
    String name,
    @Schema(description = "플랜 id *선택")
    Long planId, //null 가능
    @Schema(description = "카테고리 id *필수, 플랜 선택 시 플랜의 카테고리와 일치해야 함.")
    @NotNull(message = "카테고리를 선택해 주세요.")
    Long categoryId //필수
) {

    public TimerRequest {
        name = name().trim();
    }

    public TimerEntity toEntity(UserEntity user, PlanEntity plan, CategoryEntity category) {
        return TimerEntity.builder()
                .name(name) //빌더로 값 넘김 -> 빌더를 통해 생성자로 전달됨 -> 생성자에서 trim 처리됨
                .user_id(user)
                .plan_id(plan) //null 갈 수 O
                .category_id(category)
                .build();
        //endAt은 엔티티 빌더에서 초기화됨
    }
}
