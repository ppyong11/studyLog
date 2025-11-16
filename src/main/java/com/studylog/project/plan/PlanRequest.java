package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "계획 수정/등록 request")
public record PlanRequest (
    @Schema(description = "계획명 (30자 이내)")
    @NotBlank(message= "계획명을 입력해 주세요.")
    @Size(max= 30, message = "30자 이내로 입력해 주세요. ")
    String name,
    @Schema(description = "계획 메모")
    String memo,
    @Schema(description = "카테고리 id *필수")
    @NotNull(message = "카테고리를 선택해 주세요.")
    //json string -> long 타입 알아서 바꿔줌
    //숫자 값 아닐 시 ("abc") spring이 오류 잡아줌
    Long categoryId,
    @Schema(description = "계획 시작 날짜")
    @NotNull(message = "시작 날짜를 입력해 주세요.")
    LocalDate startDate,
    @Schema(description = "계획 종료 날짜")
    @NotNull(message = "종료 날짜를 입력해 주세요.")
    LocalDate endDate,
    @Schema(description = "계획 목표 시간 (분 단위)")
    @NotNull(message = "시간을 입력해 주세요.")
    @PositiveOrZero(message = "음수 값은 입력할 수 없습니다.")
    Integer minutes
) {
    @AssertFalse(message = "시작 날짜가 종료 날짜보다 뒤일 수 없습니다.")
    public boolean isValidDateRange() {
        return startDate.isAfter(endDate);
    }

    public PlanRequest {
        name = name().trim();
        memo = (memo() == null)? "" : memo().trim();
    }
    public PlanEntity toEntity(UserEntity user, CategoryEntity category) {
        return PlanEntity.builder()
                .user(user)
                .category(category)
                .name(name)
                .memo(memo)
                .startDate(startDate)
                .endDate(endDate)
                .minutes(minutes)
                .build();
    }
}
