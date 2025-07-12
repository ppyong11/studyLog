package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.global.exception.BadRequestException;
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
    private String plan_name;

    private String plan_memo;

    @Column(name= "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name= "end_date",nullable = false)
    private LocalDate endDate; //미지정 시 start_date와 같음

    @Column(name="plan_minutes")
    private Integer minutes; //시간 지정

    @Column(name = "plan_status",nullable = false)
    private boolean status; //계획 생성 시 0으로 설정

    @Builder
    public PlanEntity (UserEntity user, CategoryEntity category, String plan_name, String memo,
                       LocalDate startDate, LocalDate endDate, int minutes)
    {
        if(startDate.isAfter(endDate)){
            throw new BadRequestException("시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
        }
        if(minutes < 0){
            throw new BadRequestException("음수값은 입력될 수 없습니다.");
        }
        this.user = user;
        this.category = category;
        this.plan_name = plan_name.trim();
        this.plan_memo = memo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minutes = minutes; //미지정 시 0
        this.status = false;
    } //plan_id는 DB에서 자동 설정

    public void updateCategory(CategoryEntity category){
        this.category = category; //계획에 지정한 카테고리 삭제 시 "기타"로 바뀜
    }

    public void updatePlan(PlanRequest request, CategoryEntity category){
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("계획명을 입력해 주세요.");
        }
        if(request.getStartDate().isAfter(request.getEndDate())){
            throw new BadRequestException("시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
        }
        if(request.getMinutes() < 0){
            throw new BadRequestException("음수값은 입력될 수 없습니다.");
        }

        this.plan_name = request.getName().trim();
        this.category = category;
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.minutes = request.getMinutes();
    }

    public void updateStatus(boolean status){
        this.status = status;
    }
}
