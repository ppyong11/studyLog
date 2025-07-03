package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    Optional<PlanEntity> findByUserAndId(UserEntity user, Long id);

    List<PlanEntity> findByCategory(CategoryEntity category);
    List<PlanEntity> findAllByUser(UserEntity user); //유저별 게시글 조회
    List<PlanEntity> findAllByCategoryAndUser(CategoryEntity category, UserEntity user); //카테고리, 유저별 검색
    List<PlanEntity> findAllByUserAndStatus(UserEntity user, boolean status);

    @Query("select p from PlanEntity p where p.user=:user and p.startDate >= :start"
    + " and (:end is null or p.endDate <= :end)") //end가 null이면 위에 쿼리만, 아니면 end 포함
    List<PlanEntity> findPlansDate(@Param("user") UserEntity user,
                                   @Param("start") LocalDate startDate,
                                   @Param("end") LocalDate endDate);

    //조회 결과 없으면 false(null이어도), 있으면 true
    boolean existsByUserAndId(UserEntity user, Long id);
}
