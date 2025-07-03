package com.studylog.project.timer;

import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<TimerEntity, Long> {
    Optional<TimerEntity> findByUserAndId(UserEntity user, Long id);
}
