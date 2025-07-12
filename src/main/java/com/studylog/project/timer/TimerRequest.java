package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TimerRequest {
    @NotBlank(message = "타이머 제목을 입력해 주세요.")
    @Size(max= 20, message = "20자 이내로 입력해 주세요.")
    private String name; //null 가능

    private Long plan; //null 가능
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long category; //필수

    public TimerEntity toEntity(UserEntity user, PlanEntity plan, CategoryEntity category) {
        return TimerEntity.builder()
                .timerName(this.name) //빌더로 값 넘김 -> 빌더를 통해 생성자로 전달됨 -> 생성자에서 trim 처리됨
                .user_id(user)
                .plan_id(plan) //null 갈 수 O
                .category_id(category)
                .build();
        //endAt은 엔티티 빌더에서 초기화됨
    }
}
