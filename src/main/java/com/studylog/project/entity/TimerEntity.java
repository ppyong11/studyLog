package com.studylog.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name= "timer")
@Entity
public class TimerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timer_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private UserEntity user_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "plan_id")
    private PlanEntity plan_id; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(referencedColumnName = "category_id", nullable = false)
    private CategoryEntity category_id;

    @Column(nullable = false)
    private LocalDateTime start_time;

    private LocalDateTime end_time; //종료 시간, 널 허용

    @Builder
    public TimerEntity(UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id,
                       LocalDateTime start_time, LocalDateTime end_time) {
        this.user_id = user_id;
        this.plan_id = plan_id;
        this.category_id = category_id;
        this.start_time = start_time;
        this.end_time = end_time; //빌더에 값 지정 X 시 null 들어감
    }

    //수정 가능 필드: plan_id(플랜명), category_id(카테고리명)
    public void changePlan(PlanEntity newPlan, CategoryEntity generalCateogry) {
        this.plan_id= plan_id;

        if (newPlan != null) {
            this.category_id = newPlan.getCategory_id();
        }
        else{ //plan 설정 X 시
            this.category_id = generalCateogry;
        }
    }
}
