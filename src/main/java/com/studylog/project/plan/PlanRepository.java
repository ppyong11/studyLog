package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    Optional<PlanEntity> findByUserAndId(UserEntity user, Long id);
    List<PlanEntity> findByCategory(CategoryEntity category);
    void deleteAllByUser(UserEntity user);

    @Modifying //변경 쿼리라는 거 알리기
    @Query("UPDATE PlanEntity p SET p.category= :defaultCategory WHERE p.category= :deleteCategory")
    void updateCategory(@Param("deleteCategory") CategoryEntity deleteCategory,
                        @Param("defaultCategory") CategoryEntity defaultCategory);
}
