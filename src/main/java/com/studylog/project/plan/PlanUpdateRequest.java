package com.studylog.project.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class PlanUpdateRequest {
    @NotBlank(message= "계획명을 입력해 주세요.")
    private String planName;
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long category;
    @NotNull(message = "시작 날짜를 입력해 주세요.")
    private LocalDate startDate;
    @NotNull(message = "종료 날짜를 입력해 주세요.")
    private LocalDate endDate;

    private int planMinutes;
    private boolean planStatus;
}
