package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    // attributePaths에 같이 가져올 필드명을 적어주면
    // LEFT OUTER JOIN이 실행되어 한 방에 가져옴 (N+1 X)
    // mappedBy가 있어서 필드에 저장할 수 있는 것 (entity의 timer 필드)
    @EntityGraph(attributePaths = {"timer"})
    Optional<PlanEntity> findByUserAndId(UserEntity user, Long id);
    void deleteAllByUser(UserEntity user);

    @Modifying //변경 쿼리라는 거 알리기
    @Query("UPDATE PlanEntity p SET p.category= :defaultCategory WHERE p.category= :deleteCategory")
    void updateCategory(@Param("deleteCategory") CategoryEntity deleteCategory,
                        @Param("defaultCategory") CategoryEntity defaultCategory);
}
