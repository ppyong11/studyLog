package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class TimerStartReqeust {
    @Size(max= 30, message = "30자 이내로 입력해 주세요.")
    private String name; //null 가능

    private Long plan; //null 가능
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long category; //필수

    public TimerEntity toEntity(UserEntity user, PlanEntity plan, CategoryEntity category,
                                LocalDateTime startTime) {
        return TimerEntity.builder()
                .timerName(this.name)
                .user_id(user)
                .plan_id(plan)
                .category_id(category)
                .start_at(startTime)
                .build();
        //endAt은 엔티티 빌더에서 초기화됨
    }
}
