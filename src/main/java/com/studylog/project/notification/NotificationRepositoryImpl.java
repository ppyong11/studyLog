package com.studylog.project.notification;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.user.UserEntity;
import lombok.AllArgsConstructor;

import java.util.List;

import static com.studylog.project.notification.QNotificationEntity.notificationEntity;

@AllArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<NotificationResponse> findAllNotifications(UserEntity user, int page) {
        long pageSize= 10;
        long offset= (page - 1) * pageSize;

        return queryFactory
                .select(
                        Projections.constructor(
                                NotificationResponse.class,
                                notificationEntity.id,
                                notificationEntity.title,
                                notificationEntity.content,
                                notificationEntity.alertAt,
                                notificationEntity.url,
                                notificationEntity.isRead
                        ))
                .from(notificationEntity)
                .where(notificationEntity.user.eq(user))
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long totalItems(UserEntity user) {
        return queryFactory
                .select(notificationEntity.count())
                .from(notificationEntity)
                .where(notificationEntity.user.eq(user))
                .fetchOne();
    }
}
