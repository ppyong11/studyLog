package com.studylog.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.Time;

import java.time.LocalTime;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Table(name= "plan")
@Entity
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long plan_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private UserEntity user_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "category_id", nullable = false)
    private CategoryEntity category_id;

    @Column(nullable = false)
    private String plan_name;

    @Column(nullable = false)
    private Date start_date;

    @Column(nullable = false)
    private Date end_date; //미지정 시 start_date와 같음
    private LocalTime plan_minutes;

    @Column(nullable = false)
    private Boolean plan_status; //계획 생성 시 0으로 설정

    @Builder
    public PlanEntity (UserEntity user_id, CategoryEntity category_id, String plan_name,
                       Date start_date, Date end_date, LocalTime plan_minutes, Boolean plan_status)
    {
        this.user_id = user_id;
        this.category_id = category_id;
        this.plan_name = plan_name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.plan_minutes = plan_minutes;
        this.plan_status = plan_status;
    } //plan_id는 DB에서 자동 설정
}
