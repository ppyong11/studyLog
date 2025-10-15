package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.user.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<TimerEntity, Long> {
    Optional<TimerEntity> findByUserAndId(UserEntity user, Long id);

    //유저의 타이머가 맞으면 카테고리, 플랜, 랩 엔티티 반환
    @Query("select t from TimerEntity t join fetch t.category join fetch t.plan " +
            "where t.user= :userId and t.id= :timerId")
    Optional<TimerEntity> getTimerWithPlanCategory(@Param("userId") UserEntity userId,
                                                      @Param("timerId") Long timerId);

    void deleteAllByUser(UserEntity user);

    List<TimerEntity> findAllByStatus(TimerStatus status);
    Optional<TimerEntity> findByPlan(PlanEntity plan);

    boolean existsByUserAndStatus(UserEntity user, TimerStatus status);
    boolean existsByPlanId(Long planId);

    @Modifying //변경 쿼리라는 거 알리기
    @Query("UPDATE TimerEntity t SET t.category= :defaultCategory WHERE t.category= :deleteCategory")
    void updateCategory(@Param("deleteCategory") CategoryEntity deleteCategory,
                        @Param("defaultCategory") CategoryEntity defaultCategory);
}
