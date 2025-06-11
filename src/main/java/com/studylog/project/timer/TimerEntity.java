package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
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
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PlanEntity plan; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private LocalDateTime start_at;

    private LocalDateTime end_at; //종료 시간, 처음엔 null

    @Builder
    public TimerEntity(UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id,
                       LocalDateTime start_at) {
        this.user = user_id;
        this.plan = plan_id;
        this.category = category_id;
        this.start_at = start_at;
        //end_at은 자바 필드 초기값 가짐 (null)
    }

    //수정 가능 필드: plan_id(플랜명), category_id(카테고리명)
    public void changePlan(PlanEntity newPlan, CategoryEntity generalCateogry) {
        this.plan= newPlan;

        if (newPlan != null) {
            this.category = newPlan.getCategory();
        }
        else{ //plan 설정 X 시
            this.category = generalCateogry;
        }
    }

    //타이머 종료 시
    public void endTimer(LocalDateTime end_at) {
        this.end_at = end_at;
    }
}
