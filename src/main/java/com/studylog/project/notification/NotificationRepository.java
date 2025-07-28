package com.studylog.project.notification;

import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllbyUser(UserEntity user);
    List<NotificationEntity> findAllByUserAndTimer(UserEntity user, TimerEntity timer);
    Optional<NotificationEntity> findByUserAndId(UserEntity user, Long id);
}
