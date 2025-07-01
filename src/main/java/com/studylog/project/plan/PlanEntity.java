package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Table(name= "plan")
@Entity
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
    private String plan_name;

    @Column(name= "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name= "end_date",nullable = false)
    private LocalDate endDate; //미지정 시 start_date와 같음

    @Column(name="plan_minutes")
    private int minutes; //시간 지정

    @Column(name = "plan_status",nullable = false)
    private boolean status; //계획 생성 시 0으로 설정

    @Builder
    public PlanEntity (UserEntity user_id, CategoryEntity category_id, String plan_name,
                       LocalDate start_date, LocalDate end_date, int plan_minutes, boolean plan_status)
    {
        this.user = user_id;
        this.category = category_id;
        this.plan_name = plan_name;
        this.startDate = start_date;
        this.endDate = end_date;
        this.minutes = plan_minutes; //미지정 시 0
        this.status = plan_status;
    } //plan_id는 DB에서 자동 설정

    public void updateCategory(CategoryEntity category_id) {
        this.category = category_id;
    }
}
