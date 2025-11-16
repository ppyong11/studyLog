package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Table(name= "plan")
@Entity
@Slf4j
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "plan_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private String name;

    private String memo;

    @Column(name= "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name= "end_date",nullable = false)
    private LocalDate endDate; //미지정 시 start_date와 같음

    @Column(name="minutes")
    private Integer minutes; //시간 지정

    @Column(name = "is_complete",nullable = false)
    private boolean isComplete; //계획 생성 시 0으로 설정

    @OneToOne(mappedBy = "plan", cascade = CascadeType.REMOVE, optional = true)
    //타이머 삭제 로직은 따로 있어서 orphanRemoval 필요X
    private TimerEntity timer;

    @Builder
    public PlanEntity (UserEntity user, CategoryEntity category, String name, String memo,
                       LocalDate startDate, LocalDate endDate, int minutes)
    {
        this.user = user;
        this.category = category;
        this.name = name;
        this.memo = memo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minutes = minutes; //미지정 시 0
        this.isComplete = false;
    } //plan_id는 DB에서 자동 설정

    public void updateCategory(CategoryEntity category){
        this.category = category; //계획에 지정한 카테고리 삭제 시 "기타"로 바뀜
    }

    public void updatePlan(PlanRequest request, CategoryEntity category){
        this.memo= request.memo();
        this.name = request.name();
        this.category = category;
        this.startDate = request.startDate();
        this.endDate = request.endDate();
        this.minutes = request.minutes();
    }

    public void updateStatus(boolean isComplete){
        this.isComplete = isComplete;
    }
}
