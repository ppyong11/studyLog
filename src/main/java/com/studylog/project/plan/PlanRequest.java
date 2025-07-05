package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class PlanRequest {
    @NotBlank(message= "계획명을 입력해 주세요.")
    private String name;
    @NotNull(message = "카테고리를 선택해 주세요.")
    //json string -> long 타입 알아서 바꿔줌
    //숫자 값 아닐 시 ("abc") spring이 오류 잡아줌
    private Long categoryId;
    @NotNull(message = "시작 날짜를 입력해 주세요.")
    private LocalDate startDate;
    @NotNull(message = "종료 날짜를 입력해 주세요.")
    private LocalDate endDate;
    @NotNull(message = "시간을 입력해 주세요.")
    private Integer minutes;
    public PlanEntity toEntity(UserEntity user, CategoryEntity category) {
        return PlanEntity.builder()
                .user_id(user)
                .category_id(category)
                .plan_name(this.name)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .minutes(this.minutes)
                .build();
    }
}
