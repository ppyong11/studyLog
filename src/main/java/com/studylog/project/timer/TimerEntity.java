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
    @Column(name= "timer_id")
    private Long id;

    @Column(name="timer_name")
    private String timerName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PlanEntity plan; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name= "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name= "end_at")
    private LocalDateTime endAt; //종료 시간, 처음엔 null

    @Column(name= "pause_at")
    private LocalDateTime pauseAt;
    @Builder
    public TimerEntity(String timerName, UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id,
                       LocalDateTime start_at) {
        //null인데 trim하면 NPE 뜸
        this.timerName= (timerName == null)? null:timerName.trim();
        this.user = user_id;
        this.plan = plan_id;
        this.category = category_id;
        this.startAt = start_at;
        this.endAt = null;
        this.pauseAt = null;
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
    public void endTimer(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
