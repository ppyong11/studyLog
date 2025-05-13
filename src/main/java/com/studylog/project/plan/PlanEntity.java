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
    private Long plan_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private String plan_name;

    @Column(nullable = false)
    private LocalDate start_date;

    @Column(nullable = false)
    private LocalDate end_date; //미지정 시 start_date와 같음
    private LocalTime plan_minutes;

    @Column(nullable = false)
    private Boolean plan_status; //계획 생성 시 0으로 설정

    @Builder
    public PlanEntity (UserEntity user_id, CategoryEntity category_id, String plan_name,
                       LocalDate start_date, LocalDate end_date, LocalTime plan_minutes, Boolean plan_status)
    {
        this.user = user_id;
        this.category = category_id;
        this.plan_name = plan_name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.plan_minutes = plan_minutes;
        this.plan_status = plan_status;
    } //plan_id는 DB에서 자동 설정
}
