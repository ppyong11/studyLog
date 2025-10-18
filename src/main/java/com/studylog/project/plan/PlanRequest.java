package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Schema(description = "계획 수정/등록 request")
public class PlanRequest {
    @Schema(description = "계획명 (30자 이내)")
    @NotBlank(message= "계획명을 입력해 주세요.")
    @Size(max= 30, message = "30자 이내로 입력해 주세요. ")
    private String name;
    @Schema(description = "계획 메모")
    private String memo;
    @Schema(description = "카테고리 id *필수")
    @NotNull(message = "카테고리를 선택해 주세요.")
    //json string -> long 타입 알아서 바꿔줌
    //숫자 값 아닐 시 ("abc") spring이 오류 잡아줌
    private Long categoryId;
    @Schema(description = "계획 시작 날짜")
    @NotNull(message = "시작 날짜를 입력해 주세요.")
    private LocalDate startDate;
    @Schema(description = "계획 종료 날짜")
    @NotNull(message = "종료 날짜를 입력해 주세요.")
    private LocalDate endDate;
    @Schema(description = "계획 목표 시간 (분 단위)")
    @NotNull(message = "시간을 입력해 주세요.")
    private Integer minutes;

    public PlanEntity toEntity(UserEntity user, CategoryEntity category) {
        return PlanEntity.builder()
                .user(user)
                .category(category)
                .name(this.name)
                .memo(this.memo)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .minutes(this.minutes)
                .build();
    }
}
