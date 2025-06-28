package com.studylog.project.category;

import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByUserAndName(UserEntity user, String name);

    Optional<CategoryEntity> findByUserAndName(UserEntity user, String name);
}
