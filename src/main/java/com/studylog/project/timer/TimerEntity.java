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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(optional = true) //plan이 있다면 플랜 1당 타이머 1개지만, 옵셔널도 돼서
    @JoinColumn(name = "plan_id", unique = true)
    private PlanEntity plan; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    @Column(nullable = false)
    protected String name;

    @Column(name="create_at")
    protected LocalDateTime createAt;

    @Column(name= "start_at")
    protected LocalDateTime startAt;

    @Column(name= "pause_at")
    protected LocalDateTime pauseAt;

    @Column(name= "end_at")
    protected LocalDateTime endAt;

    @Column(name="elapsed", nullable = false)
    protected Long elapsed;

    @Column(name="synced_at")
    protected LocalDateTime syncedAt;

    @Column(length = 10, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    protected TimerStatus status;

    @Builder
    public TimerEntity(String timerName, UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id) {
        //null인데 trim하면 NPE 뜸
        this.name= timerName.trim();
        this.user = user_id;
        this.plan = plan_id;
        this.category = category_id;
        this.createAt = LocalDateTime.now();
        this.startAt = null;
        this.elapsed = 0L; //첫 생성 시 0초
        this.endAt = null;
        this.pauseAt = null;
        this.syncedAt = null;
        this.status = TimerStatus.READY; //생성만 하고 사용 X
    }

    //메서드는 서비스에서 실행하니까 public
    //타이머, 랩명 업데이트
    public void updateName(String name) {
        this.name= name.trim();
    }

    public void start(){
        this.startAt = LocalDateTime.now();
        this.status = TimerStatus.RUNNING;
        this.syncedAt = null;
    }

    public void pause(){
        this.pauseAt = LocalDateTime.now();
        this.status = TimerStatus.PAUSED;
    }

    public void end(LocalDateTime endAt){
        this.endAt = endAt;
        status = TimerStatus.ENDED;
    }

    //리셋
    public void reset(){
        this.startAt= null;
        this.pauseAt= null;
        this.endAt= null;
        this.elapsed= 0L;
        this.syncedAt= null;
        this.status= TimerStatus.READY;
    }

    //누적 시간
    public void updateElapsed(Long elapsed){
        this.elapsed= elapsed;
    }

    //동기화 시간
    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }


    //타이머 계획-카테고리 업데이트
    public void updatePlan(PlanEntity plan){
        this.plan = plan; //null도 들어갈 수 o
    }
    public void updateCategory(CategoryEntity category){
        this.category = category;
    }

}
