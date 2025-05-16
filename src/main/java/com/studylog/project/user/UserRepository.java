package com.studylog.project.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findById(String id);

    boolean existsByEmail(String email);
    boolean existsById(String id);
    boolean existsByNickname(String nickname);

}
