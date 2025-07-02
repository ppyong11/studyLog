package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    Optional<PlanEntity> findByUserAndId(UserEntity user, Long id);

    List<PlanEntity> findByCategory(CategoryEntity category);
    List<PlanEntity> findAllByUser(UserEntity user); //유저별 게시글 조회
    List<PlanEntity> findAllByCategoryAndUser(CategoryEntity category, UserEntity user); //카테고리, 유저별 검색
    List<PlanEntity> findAllByUserAndStatus(UserEntity user, boolean status);
}
