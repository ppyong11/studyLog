package com.studylog.project.timer;

import com.studylog.project.Lap.LapEntity;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.global.domain.TimerLapBaseEntity;
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
public class TimerEntity extends TimerLapBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "timer_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(optional = true) //plan이 있다면 플랜 1당 타이머 1개지만, 옵셔널도 돼서
    @JoinColumn(name = "plan_id", unique = true)
    private PlanEntity plan; //플랜명 널 허용 (단순 기록)

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @OneToMany(mappedBy = "timer", cascade = CascadeType.REMOVE)
    private List<LapEntity> laps= new ArrayList<>(); //초기화는 생성할 때만 쓰는 거라 repo로 불러올 땐 잘 채워짐

    @Builder
    public TimerEntity(String timerName, UserEntity user_id, PlanEntity plan_id, CategoryEntity category_id) {
        //null인데 trim하면 NPE 뜸
        this.name= timerName.trim();
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

    //타이머 계획-카테고리 업데이트
    public void updatePlan(PlanEntity plan){
        this.plan = plan; //null도 들어갈 수 o
    }
    public void updateCategory(CategoryEntity category){
        this.category = category;
    }
}
