package com.studylog.project.repository;

import com.studylog.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsById(String id);
    boolean existsByNickname(String nickname);

}
