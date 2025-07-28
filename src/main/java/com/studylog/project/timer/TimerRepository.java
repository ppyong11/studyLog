package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<TimerEntity, Long> {
    Optional<TimerEntity> findByUserAndId(UserEntity user, Long id);
    void deleteAllByUser(UserEntity user);

    List<TimerEntity> findAllByStatus(TimerStatus status);
    List<TimerEntity> findAllByCategory(CategoryEntity category);
    TimerEntity findByPlan(PlanEntity plan);

    boolean existsByUserAndStatus(UserEntity user, TimerStatus status);
    boolean existsByPlanId(Long planId);
}
