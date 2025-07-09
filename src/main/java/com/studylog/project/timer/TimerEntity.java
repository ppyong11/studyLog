package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name="timer_name", nullable = false)
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

    @Column(name= "create_date")
    private LocalDate createDate;

    @Column(name= "start_at")
    private LocalDateTime startAt;

    @Column(name= "restart_at")
    private LocalDateTime restartAt;

    @Column(name= "end_at")
    private LocalDateTime endAt; //종료 시간, 처음엔 null

    @Column(name= "pause_at")
    private LocalDateTime pauseAt;

    @Column(name= "synced_at")
    private LocalDateTime syncedAt;

    @Column(name="elapsed", nullable = false)
    private Long elapsedSecond;

    @Column(length = 10, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private TimerStatus status;

    @Builder
    public TimerEntity(String timerName, UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id) {
        //null인데 trim하면 NPE 뜸
        this.timerName= timerName.trim();
        this.user = user_id;
        this.plan = plan_id;
        this.category = category_id;
        this.createDate = LocalDate.now();
        this.startAt = null;
        this.elapsedSecond = 0L; //첫 생성 시 0초
        this.restartAt = null;
        this.endAt = null;
        this.pauseAt = null;
        this.syncedAt = null;
        this.status = TimerStatus.READY; //생성만 하고 사용 X
    }

    //타이머 업데이트
    public void updateTimer(TimerRequest request, PlanEntity plan, CategoryEntity category){
        this.timerName= timerName.trim();
        this.plan = plan; //null도 들어갈 수 o
        //플랜이 없으면 카테고리 알아서 설정, 있다면 플랜 카테고리 따라감
        this.category = plan == null? category : plan.getCategory();
    }
    //타이머 첫 시작 시
    public void startTimer() {
        this.startAt = LocalDateTime.now();
        this.status = TimerStatus.RUNNING;
    }

    //타이머 시작 시
    public void updateRestartTimer(){
        this.restartAt = LocalDateTime.now();
        status = TimerStatus.RUNNING;
    }

    //타이머 종료 시
    public void updateEndTimer(LocalDateTime endAt) {
        this.endAt = endAt;
        status = TimerStatus.RUNNING;
    }

    //타이머 정지 시
    public void updatePauseTimer() {
        this.pauseAt = LocalDateTime.now();
        status = TimerStatus.PAUSED;
    }

    //누적 시간
    public void updateElapsedSecond(long elapsedSecond) {
        this.elapsedSecond = elapsedSecond;
    }

    //동기화 시간
    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }

    //카테고리 업데이트
    public void updateCategory(CategoryEntity category) {
        this.category = category;
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
}
