package com.studylog.project.timer;

import com.studylog.project.Lap.LapEntity;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(optional = true) //plan이 있다면 플랜 1당 타이머 1개지만, 옵셔널도 돼서
    @JoinColumn(name = "plan_id", unique = true)
    private PlanEntity plan; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name= "create_date")
    private LocalDate createDate;

    @Column(name= "start_at")
    private LocalDateTime startAt;

    @Column(name= "end_at")
    private LocalDateTime endAt; //종료 시간, 처음엔 null

    @Column(name= "pause_at")
    private LocalDateTime pauseAt;

    @Column(name= "synced_at")
    private LocalDateTime syncedAt;

    @Column(name="elapsed", nullable = false)
    private Long elapsed;

    @Column(length = 10, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private TimerStatus status;

    @OneToMany(mappedBy = "timer", cascade = CascadeType.REMOVE)
    private List<LapEntity> laps= new ArrayList<>(); //초기화는 생성할 때만 쓰는 거라 repo로 불러올 땐 잘 채워짐

    @Builder
    public TimerEntity(String timerName, UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id) {
        //null인데 trim하면 NPE 뜸
        this.timerName= timerName.trim();
        this.user = user_id;
        this.plan = plan_id;
        this.category = category_id;
        this.createDate = LocalDate.now();
        this.startAt = null;
        this.elapsed = 0L; //첫 생성 시 0초
        this.endAt = null;
        this.pauseAt = null;
        this.syncedAt = null;
        this.status = TimerStatus.READY; //생성만 하고 사용 X
    }

    //타이머 이름 업데이트
    public void updateTimerName(String name){
        this.timerName= name.trim();
    }

    //타이머 계획-카테고리 업데이트
    public void updatePlan(PlanEntity plan){
        this.plan = plan; //null도 들어갈 수 o
    }
    public void updateCategory(CategoryEntity category){
        this.category = category;
    }

    //타이머 시작 시
    public void startTimer() {
        this.startAt = LocalDateTime.now();
        this.status = TimerStatus.RUNNING;
        this.syncedAt = null;
    }

    //타이머 종료 시
    public void updateEndTimer(LocalDateTime endAt) {
        this.endAt = endAt;
        status = TimerStatus.ENDED;
    }

    //타이머 리셋 시
    public void resetTimer() {
        this.startAt = null;
        this.pauseAt = null;
        this.syncedAt = null;
        this.elapsed = 0L;
        this.status = TimerStatus.READY;
    }

    //타이머 정지 시
    public void updatePauseTimer() {
        this.pauseAt = LocalDateTime.now();
        status = TimerStatus.PAUSED;
    }

    //누적 시간
    public void updateElapsed(long elapsedSecond) {
        this.elapsed = elapsedSecond;
    }

    //동기화 시간
    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }
}
