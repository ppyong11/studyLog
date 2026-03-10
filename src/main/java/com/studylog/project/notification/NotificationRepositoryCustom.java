package com.studylog.project.notification;

import com.studylog.project.user.UserEntity;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<NotificationResponse> findAllNotifications(UserEntity user, int page);
    Long totalItems(UserEntity user);
}
